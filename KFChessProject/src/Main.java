import bus.GameBootstrapper;
import client.GameClient;
import engine.GameEngine;
import io.BoardParser;
import io.BoardPrinter;
import models.Board;
import models.GameState;
import realtime.RealTimeArbiter;
import input.Controller;
import input.GameClickHandler;
import view.FrameRenderer;
import view.HomeDialog;
import view.Renderer;
import view.RenderSnapshot;
import view.RoomDialog;
import view.SearchingDialog;
import view.SnapshotFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    private static final int CELL_SIZE = 100;
    private static final String WHITE_NAME = "Player White";
    private static final String BLACK_NAME = "Player Black";
    private static final boolean OBSERVER_MODE = false;
    private static final int TICK_MS = 16;

    private static final String STARTING_BOARD =
            "bR bN bB bK bQ bB bN bR\n" +
                    "bP bP bP bP bP bP bP bP\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    ".  .  .  .  .  .  .  .\n" +
                    "wP wP wP wP wP wP wP wP\n" +
                    "wR wN wB wK wQ wB wN wR";

    // מצב הסיבוב הנוכחי - מוחלף בכל startRound(), נקרא מה-Timer (EDT) וממתודות אחרות
    private static final AtomicReference<Board> boardRef = new AtomicReference<>();
    private static final AtomicReference<GameState> gameStateRef = new AtomicReference<>();
    private static final AtomicReference<Controller> controllerRef = new AtomicReference<>();
    private static final AtomicReference<FrameRenderer> frameRendererRef = new AtomicReference<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        GameClient wsClient;
        Renderer renderer;

        try {
            BufferedReader loginReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = loginReader.readLine();

            System.out.print("Enter your password: ");
            String password = loginReader.readLine();

            wsClient = new GameClient(new java.net.URI("ws://localhost:8887"));
            wsClient.connectBlocking();
            wsClient.send("LOGIN " + username + " " + password);

            String loginReply = wsClient.awaitLoginReply();
            if (loginReply.startsWith("LOGIN_FAILED")) {
                System.out.println("ERROR " + loginReply);
                return;
            }

            renderer = new Renderer("resources/pieces_classic", CELL_SIZE);
            renderer.initWindow(8, 8);

            // "LOGIN_OK <rating>" (login רגיל) או "LOGIN_OK <rating> RECONNECTED <role>" (חזרה למשחק פעיל)
            String[] loginParts = loginReply.split(" ");
            int rating = Integer.parseInt(loginParts[1]);

            if (loginParts.length >= 4 && loginParts[2].equals("RECONNECTED")) {
                String role = loginParts[3];
                System.out.println("Reconnected (rating " + rating + ") - resuming your game as " + role);
                startRound(wsClient, renderer);
            } else {
                chooseGameMode(wsClient, renderer);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("ERROR " + e.getMessage());
            return;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // thread ייעודי לסיבובים 2+: ממתין ל-RETURN_TO_QUEUE ואז חוזר על playRound.
        // חייב thread נפרד (לא ה-network thread, לא ה-EDT) כדי לא לחסום אף אחד מהם.
        Thread rematchLoop = new Thread(() -> {
            try {
                while (true) {
                    wsClient.awaitReturnToQueue();
                    playRound(wsClient, renderer);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "rematch-loop");
        rematchLoop.setDaemon(true);
        rematchLoop.start();

        Timer gameTimer = new Timer(TICK_MS, e -> {
            Controller controller = controllerRef.get();
            FrameRenderer frameRenderer = frameRendererRef.get();
            if (controller != null && frameRenderer != null) {
                controller.update(TICK_MS);
                frameRenderer.renderNow();
            }
        });
        gameTimer.start();

        // לולאת-הפקודות מהקונסולה נשארת - שימושית לבדיקות (click/jump/print board)
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            String cmd = line.trim();
            if (cmd.isEmpty()) continue;

            String[] parts = cmd.split("\\s+");
            String op = parts[0].toLowerCase();
            Controller controller = controllerRef.get();
            if (controller == null) continue;

            if (op.equals("click") && parts.length == 3) {
                controller.handleMouseClick(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            } else if (op.equals("wait") && parts.length == 2) {
                try {
                    Thread.sleep(Long.parseLong(parts[1]));
                } catch (InterruptedException ignored) {}

            } else if (op.equals("jump") && parts.length == 3) {
                controller.handleJumpCommand(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            } else if (op.equals("print") && parts.length == 2 && parts[1].equalsIgnoreCase("board")) {
                models.GameSnapshot printSnapshot = new models.GameSnapshot(boardRef.get(), gameStateRef.get());
                System.out.println(BoardPrinter.print(printSnapshot));
            }
        }
    }

    // מסך בית: Quick Play (matchmaking רגיל) או Room (create/join). CANCEL ב-RoomDialog חוזר לכאן.
    private static void chooseGameMode(GameClient wsClient, Renderer renderer) throws InterruptedException {
        while (true) {
            HomeDialog.Choice choice = new HomeDialog().open();

            if (choice == HomeDialog.Choice.QUICK_PLAY) {
                playRound(wsClient, renderer);
                return;
            }

            if (handleRoomChoice(wsClient, renderer)) {
                return;
            }
            // CANCEL, JOIN_FAILED, או קוד ריק - חוזרים למסך הבית ומנסים שוב
        }
    }

    // מחזירה true אם המשחק התחיל בפועל (create/join הצליחו), false אם צריך לחזור למסך הבית
    private static boolean handleRoomChoice(GameClient wsClient, Renderer renderer) throws InterruptedException {
        RoomDialog.Result roomChoice = new RoomDialog().open();

        if (roomChoice.action() == RoomDialog.Action.CANCEL) {
            wsClient.send("CANCEL");
            return false;
        }

        if (roomChoice.action() == RoomDialog.Action.CREATE) {
            wsClient.send("CREATE_ROOM");
        } else {
            String roomId = roomChoice.roomId();
            if (roomId == null || roomId.isEmpty()) {
                return false;
            }
            wsClient.send("JOIN_ROOM " + roomId);
        }

        String roomReply = wsClient.awaitRoomReply();

        if (roomReply.startsWith("JOIN_FAILED")) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null,
                            "Room not found. Please check the code and try again.",
                            "Join Failed", JOptionPane.WARNING_MESSAGE)
            );
            return false;
        }

        if (roomReply.startsWith("ROOM_CREATED ")) {
            String roomId = roomReply.substring("ROOM_CREATED ".length());
            try {
                SwingUtilities.invokeAndWait(() ->
                        JOptionPane.showMessageDialog(null,
                                "Room created! Share this code with your opponent:\n\n" + roomId,
                                "Room Created", JOptionPane.INFORMATION_MESSAGE)
                );
            } catch (Exception ignored) {}
        } else {
            System.out.println("Joined room: " + roomReply.substring("ROOM_JOINED ".length()));
        }

        String matchReply = wsClient.awaitMatchReply();
        System.out.println("You are " + matchReply.substring("ROLE ".length()));

        startRound(wsClient, renderer);
        return true;
    }

    // שולח PLAY, מציג SearchingDialog, ומנסה שוב אוטומטית (בלי פופ-אפ) על NO_MATCH
    private static void playRound(GameClient wsClient, Renderer renderer) throws InterruptedException {
        SearchingDialog searchingDialog = new SearchingDialog();
        searchingDialog.open("Waiting for game to start (matching, or resuming an existing game)...");

        String matchReply;
        do {
            wsClient.send("PLAY");
            matchReply = wsClient.awaitMatchReply();
        } while (matchReply.equals("NO_MATCH"));

        searchingDialog.close();
        System.out.println("Match found - you are " + matchReply.substring("ROLE ".length()));

        startRound(wsClient, renderer);
    }

    // בונה board/engine/gameState/controller/frameRenderer טריים לסיבוב חדש, ומחליף אותם ב-AtomicReference-ים
    private static void startRound(GameClient wsClient, Renderer renderer) {
        Board board = BoardParser.parse(STARTING_BOARD);
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameState gameState = new GameState();
        GameEngine engine = GameBootstrapper.buildEngine(board, arbiter, gameState);

        wsClient.startNewRound(engine, gameState);

        Controller controller = new Controller(engine, board, wsClient);
        FrameRenderer frameRenderer = () -> {
            RenderSnapshot snap = SnapshotFactory.build(
                    board, engine, arbiter, controller.getSelectedPosition(), CELL_SIZE,
                    gameState, WHITE_NAME, BLACK_NAME, wsClient.getDisconnectSecondsLeft(),
                    wsClient.getDisconnectTotalSeconds(), wsClient.getReturnCountdownSecondsLeft(),
                    wsClient.getReturnCountdownTotalSeconds());
            renderer.renderFrame(snap);
        };

        boardRef.set(board);
        gameStateRef.set(gameState);
        controllerRef.set(controller);
        frameRendererRef.set(frameRenderer);

        if (!OBSERVER_MODE) {
            GameClickHandler clickHandler = new GameClickHandler(controller, frameRenderer);
            renderer.setOnClick(clickHandler);
            input.JumpClickHandler jumpClickHandler = new input.JumpClickHandler(controller, frameRenderer);
            renderer.setOnRightClick(jumpClickHandler);
        }

        frameRenderer.renderNow();
    }
}
