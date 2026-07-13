
import engine.GameEngine;
import io.BoardParser;
import io.BoardPrinter;
import models.Board;
import models.GameState;
import realtime.RealTimeArbiter;
import input.Controller;
import models.GameSnapshot;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. אתחול ראשוני - הלוח נטען מטקסט (שימוש ב-BoardParser!)
        String initialBoardLayout =
                "bR bN bB bQ bK bB bN bR\n" +
                        "bP bP bP bP bP bP bP bP\n" +
                        ".  .  .  .  .  .  .  .\n" +
                        ".  .  .  .  .  .  .  .\n" +
                        ".  .  .  .  .  .  .  .\n" +
                        ".  .  .  .  .  .  .  .\n" +
                        "wP wP wP wP wP wP wP wP\n" +
                        "wR wN wB wQ wK wB wN wR";

        Board board = BoardParser.parse(initialBoardLayout);
        GameState gameState = new GameState();
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameEngine engine = new GameEngine(board, arbiter, gameState);
        Controller controller = new Controller(engine, board);

        Scanner scanner = new Scanner(System.in);
        long currentTime = 0;

        System.out.println("--- Kung Fu Chess Initialized ---");
        System.out.println(BoardPrinter.print(new GameSnapshot(board, gameState)));

        // 2. לולאת המשחק - עכשיו היא מבוססת על הקלט המפורמט שלך
        while (!gameState.isGameOver()) {
            System.out.println("\nEnter command (move x1 y1 x2 y2 / jump x y / tick):");
            String cmd = scanner.next();

            if (cmd.equals("move")) {
                int x1 = scanner.nextInt(); int y1 = scanner.nextInt();
                int x2 = scanner.nextInt(); int y2 = scanner.nextInt();
                controller.handleMouseClick(x1, y1, currentTime);
                controller.handleMouseClick(x2, y2, currentTime);
            } else if (cmd.equals("jump")) {
                int x = scanner.nextInt(); int y = scanner.nextInt();
                controller.handleJumpCommand(x, y, currentTime);
            } else if (cmd.equals("tick")) {
                currentTime += 1000;
                controller.update(currentTime);
                // הדפסה מעודכנת בעזרת ה-Printer
                System.out.println(BoardPrinter.print(new GameSnapshot(board, gameState)));
            }
        }
        System.out.println("Game Over!");
    }
}