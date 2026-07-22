package server;

import bus.GameBootstrapper;
import engine.GameEngine;
import io.BoardParser;
import models.Board;
import models.GameState;
import models.Position;
import org.java_websocket.WebSocket;
import realtime.RealTimeArbiter;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * עוטפת משחק בודד (room): הלוח/מנוע/מצב שלו, מי משתתף בו (כולל כמה SPECTATOR-ים),
 * ואת ה-managers הספציפיים למשחק הזה (reconnect, rematch).
 * scheduler משותף לכל ה-sessions (לא נבנה מחדש לכל room - חוסך threads).
 */
public class GameSession {

    private static final long TICK_MS = 16;

    private static final String STARTING_BOARD =
            "bR bN bB bK bQ bB bN bR\n" +
                    "bP bP bP bP bP bP bP bP\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    "wP wP wP wP wP wP wP wP\n" +
                    "wR wN wB wK wQ wB wN wR";

    private final String roomId;
    private final boolean autoRequeueOnGameOver;
    private final ScheduledExecutorService scheduler;

    private final PlayerRegistry playerRegistry = new PlayerRegistry();

    // כל החיבורים ששייכים ל-room הזה - WHITE + BLACK + כל ה-SPECTATOR-ים, לצורך broadcast
    private final Set<WebSocket> members = ConcurrentHashMap.newKeySet();

    private volatile Board board;
    private volatile GameEngine engine;

    private final ReconnectManager reconnectManager;
    private RematchService rematchService;

    public GameSession(String roomId, boolean autoRequeueOnGameOver, ScheduledExecutorService scheduler) {
        this.roomId = roomId;
        this.autoRequeueOnGameOver = autoRequeueOnGameOver;
        this.scheduler = scheduler;
        this.reconnectManager = new ReconnectManager(playerRegistry, scheduler, () -> engine.getBus());
    }

    public String getRoomId() {
        return roomId;
    }

    public PlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }

    public ReconnectManager getReconnectManager() {
        return reconnectManager;
    }

    public void addMember(WebSocket conn) {
        members.add(conn);
    }

    public void removeMember(WebSocket conn) {
        members.remove(conn);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public void broadcastToMembers(String message) {
        for (WebSocket conn : members) {
            try {
                conn.send(message);
            } catch (Exception e) {
                System.out.println("[ROOM " + roomId + "] could not send to a member: " + e.getMessage());
            }
        }
    }

    public String getUsernameByRole(PlayerRole targetRole) {
        return playerRegistry.getUsernameByRole(targetRole);
    }

    // בונה board/engine חדשים לגמרי לתחילת משחק (ראשון, או rematch באותו room)
    public void startMatch() {
        this.board = BoardParser.parse(STARTING_BOARD);
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameState gameState = new GameState();
        this.engine = GameBootstrapper.buildEngine(board, arbiter, gameState);

        engine.getBus().subscribe("piece.captured", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("move.completed", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("game.over", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("game.over", new RatingUpdateSubscriber(this));
        engine.getBus().subscribe("piece.jumped", new NetworkBroadcastSubscriber(this));

        ActivityLogSubscriber activityLog = new ActivityLogSubscriber(roomId);
        engine.getBus().subscribe("move.completed", activityLog);
        engine.getBus().subscribe("piece.captured", activityLog);
        engine.getBus().subscribe("piece.jumped", activityLog);
        engine.getBus().subscribe("game.over", activityLog);

        if (autoRequeueOnGameOver) {
            if (rematchService == null) {
                // אין צורך "להתחיל מחדש" את אותו room - הלקוחות מקבלים RETURN_TO_QUEUE
                // ושולחים PLAY חדש בעצמם, ש-MatchmakingService מנתב ל-room אנונימי חדש.
                // ה-room הישן מתנקה אוטומטית כששני הצדדים עוזבים אותו (leaveCurrentRoom).
                rematchService = new RematchService(playerRegistry, scheduler, () -> {});
            }
            engine.getBus().subscribe("game.over", rematchService);
        }

        System.out.println("[ROOM " + roomId + "] match ready");
    }

    public void startGameLoop() {
        Timer gameLoop = new Timer();
        gameLoop.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                engine.waitMs(TICK_MS);
            }
        }, 0, TICK_MS);
    }

    public void handleGameMessage(WebSocket conn, String message) {
        PlayerRole role = playerRegistry.getRole(conn);
        if (role == null || role == PlayerRole.SPECTATOR) {
            System.out.println("[ROOM " + roomId + "] ignored move - no active role (not matched yet or spectator)");
            return;
        }

        if (reconnectManager.isPausedForDisconnect()) {
            System.out.println("[ROOM " + roomId + "] move rejected - game paused, opponent disconnected");
            return;
        }

        if (message.length() == 4 && message.charAt(1) == 'J') {
            handleJump(message, role);
            return;
        }

        handleMove(message, role);
    }

    private void handleJump(String message, PlayerRole role) {
        try {
            Position pos = CommandParser.parseJump(message, board);
            char colorChar = message.charAt(0);
            boolean isWhiteCommand = colorChar == 'W';
            boolean roleMatches = (role == PlayerRole.WHITE && isWhiteCommand)
                    || (role == PlayerRole.BLACK && !isWhiteCommand);
            if (!roleMatches) {
                System.out.println("[ROOM " + roomId + "] rejected jump: " + message + " - wrong color for role " + role);
                return;
            }
            engine.triggerJump(pos);
        } catch (IllegalArgumentException e) {
            System.out.println("[ROOM " + roomId + "] bad jump command: " + message);
        }
    }

    private void handleMove(String message, PlayerRole role) {
        try {
            Position[] positions = CommandParser.parseMove(message, board);

            char colorChar = message.charAt(0);
            boolean isWhiteCommand = colorChar == 'W';
            boolean roleMatches = (role == PlayerRole.WHITE && isWhiteCommand)
                    || (role == PlayerRole.BLACK && !isWhiteCommand);

            if (!roleMatches) {
                System.out.println("[ROOM " + roomId + "] rejected: " + message + " - wrong color for role " + role);
                return;
            }

            engine.tryMove(positions[0], positions[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("[ROOM " + roomId + "] bad command: " + message);
        }
    }

    public void onDisconnect(WebSocket conn) {
        removeMember(conn);
        PlayerRole role = playerRegistry.getRole(conn);

        if (role == PlayerRole.WHITE || role == PlayerRole.BLACK) {
            reconnectManager.onPlayerDisconnected(conn, role);
        } else {
            playerRegistry.removeRole(conn);
        }
    }
}
