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
import view.Renderer;
import view.RenderSnapshot;
import view.SnapshotFactory;
import javax.swing.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

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

    public static void main(String[] args) throws IOException, InterruptedException {

        Board board;
        RealTimeArbiter arbiter;
        GameState gameState;
        GameEngine engine;
        Controller controller;
        Renderer renderer;
        FrameRenderer frameRenderer;
        GameClickHandler clickHandler;

        try {
            board = BoardParser.parse(STARTING_BOARD);
            arbiter = new RealTimeArbiter();
            gameState = new GameState();
            engine = GameBootstrapper.buildEngine(board, arbiter, gameState);

            BufferedReader loginReader = new java.io.BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = loginReader.readLine();

            System.out.print("Enter your password: ");
            String password = loginReader.readLine();

            GameClient wsClient = new GameClient(new java.net.URI("ws://localhost:8887"), engine);
            wsClient.connectBlocking();
            wsClient.send("LOGIN " + username + " " + password);

            controller = new Controller(engine, board, wsClient);
            renderer = new Renderer("resources/pieces_classic", CELL_SIZE);
            renderer.initWindow(board.getWidth(), board.getHeight());

            frameRenderer = () -> {
                RenderSnapshot snap = SnapshotFactory.build(
                        board, engine, arbiter, controller.getSelectedPosition(), CELL_SIZE,
                        gameState, WHITE_NAME, BLACK_NAME);
                renderer.renderFrame(snap);
            };

            frameRenderer.renderNow();

        } catch (IllegalArgumentException e) {
            System.out.println("ERROR " + e.getMessage());
            return;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (!OBSERVER_MODE) {
            clickHandler = new GameClickHandler(controller, frameRenderer);
            renderer.setOnClick(clickHandler);

            input.JumpClickHandler jumpClickHandler = new input.JumpClickHandler(controller, frameRenderer);
            renderer.setOnRightClick(jumpClickHandler);
        } else {
            clickHandler = null;
        }

        Timer gameTimer = new Timer(TICK_MS, e -> {
            controller.update(TICK_MS);
            frameRenderer.renderNow();
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

            if (op.equals("click") && parts.length == 3) {
                controller.handleMouseClick(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            } else if (op.equals("wait") && parts.length == 2) {
                try {
                    Thread.sleep(Long.parseLong(parts[1]));
                } catch (InterruptedException ignored) {}

            } else if (op.equals("jump") && parts.length == 3) {
                controller.handleJumpCommand(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            } else if (op.equals("print") && parts.length == 2 && parts[1].equalsIgnoreCase("board")) {
                models.GameSnapshot printSnapshot = new models.GameSnapshot(board, gameState);
                System.out.println(BoardPrinter.print(printSnapshot));
            }
        }
    }
}