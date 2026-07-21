package server;

import bus.GameBootstrapper;
import engine.GameEngine;
import io.BoardParser;
import models.Board;
import models.GameState;
import models.Position;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import realtime.RealTimeArbiter;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GameServer extends WebSocketServer {

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

    private volatile GameEngine engine;
    private volatile Board board;

    private final PlayerRegistry playerRegistry = new PlayerRegistry();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final MatchmakingService matchmaking;
    private final ReconnectManager reconnectManager;
    private final RematchService rematchService;

    public GameServer(int port) {
        super(new InetSocketAddress(port));
        this.matchmaking = new MatchmakingService(playerRegistry, scheduler);
        this.reconnectManager = new ReconnectManager(playerRegistry, scheduler, () -> engine.getBus());
        this.rematchService = new RematchService(playerRegistry, scheduler, this::startNewMatch);

        startNewMatch();
        startGameLoop();
    }

    private void startNewMatch() {
        this.board = BoardParser.parse(STARTING_BOARD);
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameState gameState = new GameState();
        this.engine = GameBootstrapper.buildEngine(board, arbiter, gameState);

        engine.getBus().subscribe("piece.captured", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("move.completed", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("game.over", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("game.over", new RatingUpdateSubscriber(this));
        engine.getBus().subscribe("piece.jumped", new NetworkBroadcastSubscriber(this));
        engine.getBus().subscribe("game.over", rematchService);

        System.out.println("[SERVER] new match board ready");
    }

    private void startGameLoop() {
        Timer gameLoop = new Timer();
        gameLoop.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                engine.waitMs(TICK_MS);
            }
        }, 0, TICK_MS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[SERVER] client connected: " + conn.getRemoteSocketAddress());
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        PlayerRole role = playerRegistry.getRole(conn);
        matchmaking.removeFromQueue(conn);

        if (role == PlayerRole.WHITE || role == PlayerRole.BLACK) {
            reconnectManager.onPlayerDisconnected(conn, role);
        } else {
            playerRegistry.removeRole(conn);
        }

        System.out.println("[SERVER] client disconnected: " + conn.getRemoteSocketAddress()
                + " | code=" + code + " reason=" + reason + " remote=" + remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[SERVER] received: " + message);

        if (message.startsWith("LOGIN ")) {
            handleLogin(conn, message);
            return;
        }

        if (message.equals("PLAY")) {
            matchmaking.handlePlayRequest(conn);
            return;
        }

        PlayerRole role = playerRegistry.getRole(conn);
        if (role == null || role == PlayerRole.SPECTATOR) {
            System.out.println("[SERVER] ignored move - no active role (not matched yet or spectator)");
            return;
        }

        if (reconnectManager.isPausedForDisconnect()) {
            System.out.println("[SERVER] move rejected - game paused, opponent disconnected");
            return;
        }

        if (message.length() == 4 && message.charAt(1) == 'J') {          // <-- חדש: זיהוי-קפיצה
            handleJump(message, role);
            return;
        }

        handleMove(message, role);
    }

    private void handleLogin(WebSocket conn, String message) {
        String[] parts = message.substring(6).split(" ");
        if (parts.length != 2) {
            conn.send("LOGIN_FAILED bad format");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        boolean userExists = DatabaseManager.userExists(username);
        boolean success = userExists
                ? DatabaseManager.verifyLogin(username, password)
                : DatabaseManager.registerUser(username, password);

        if (!success) {
            conn.send("LOGIN_FAILED wrong password");
            System.out.println("[SERVER] login failed for " + username);
            return;
        }

        playerRegistry.setName(conn, username);
        int rating = DatabaseManager.getRating(username);

        PlayerRole restoredRole = reconnectManager.tryReconnect(username, conn);
        if (restoredRole != null) {
            System.out.println("[SERVER] " + username + " logged in (rating " + rating + ") - reconnected as " + restoredRole);
            conn.send("LOGIN_OK " + rating + " RECONNECTED " + restoredRole);
        } else {
            System.out.println("[SERVER] " + username + " logged in (rating " + rating + ")");
            conn.send("LOGIN_OK " + rating);
        }
    }

    private void handleJump(String message, PlayerRole role) {
        try {
            Position pos = CommandParser.parseJump(message, board);
            char colorChar = message.charAt(0);
            boolean isWhiteCommand = colorChar == 'W';
            boolean roleMatches = (role == PlayerRole.WHITE && isWhiteCommand)
                    || (role == PlayerRole.BLACK && !isWhiteCommand);
            if (!roleMatches) {
                System.out.println("[SERVER] rejected jump: " + message + " - wrong color for role " + role);
                return;
            }
            engine.triggerJump(pos);
        } catch (IllegalArgumentException e) {
            System.out.println("[SERVER] bad jump command: " + message);
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
                System.out.println("[SERVER] rejected: " + message + " - wrong color for role " + role);
                return;
            }

            engine.tryMove(positions[0], positions[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("[SERVER] bad command: " + message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("[SERVER] error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("[SERVER] started successfully");
    }

    public String getUsernameByRole(PlayerRole targetRole) {
        return playerRegistry.getUsernameByRole(targetRole);
    }
}
