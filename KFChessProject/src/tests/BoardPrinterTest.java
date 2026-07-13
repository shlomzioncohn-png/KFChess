import io.BoardParser;
import io.BoardPrinter;
import models.Board;
import models.GameSnapshot;
import models.GameState;
import models.enums.PieceColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardPrinterTest {

    @Test
    void printsActiveGameCorrectly() {
        Board board = BoardParser.parse("wP .\n. bR");
        GameState state = new GameState();
        GameSnapshot snapshot = new GameSnapshot(board, state);

        String expectedOutput = "wP .\n. bR\nGame is active";
        String output = BoardPrinter.print(snapshot);

        assertEquals(expectedOutput.trim(), output.trim());
    }

    @Test
    void printsGameOverWithWinner() {
        Board board = BoardParser.parse(". .\n. .");
        GameState state = new GameState();
        state.setGameOver(true);
        state.setWinner(PieceColor.WHITE);
        GameSnapshot snapshot = new GameSnapshot(board, state);

        String output = BoardPrinter.print(snapshot);

        assertTrue(output.contains("Game Over! Winner: WHITE"),
                "כאשר המשחק נגמר, הפלט חייב לכלול את שם המנצח");
    }

    @Test
    void nullSnapshotReturnsEmptyString() {
        assertEquals("", BoardPrinter.print(null));
    }
}
