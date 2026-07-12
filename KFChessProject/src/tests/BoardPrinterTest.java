import io.BoardPrinter;
import models.*;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardPrinterTest {

    /**
     * מייצר לוח וסטטוס משחק בזיכרון, ומוודא שהמדפיס יודע לתרגם אותם למחרוזת  בצורה מדויקת
     * – עם האותיות הנכונות לכל כלי, נקודות לתאים ריקים,ןכו...
     */
    @Test
    public void testPrintActiveGame() {
        Board board = new MatrixBoard(2, 2);
        Position pos = new Position(0, 0);
        Piece whiteKing = new Piece("k1", PieceColor.WHITE, PieceType.KING, pos);
        board.addPiece(pos, whiteKing);

        GameState state = new GameState();
        GameSnapshot snapshot = new GameSnapshot(board, state);

        String output = BoardPrinter.print(snapshot);

        String expectedOutput = "wK .\n" +
                ". .\n" +
                "Game is active";

        assertEquals(expectedOutput.trim(), output.trim());
    }

    /**
     * מוודא שאם פונקציית ההדפסה מקבלת בטעות אובייקט ריק , היא לא תזרוק שגיאת קריסה
     * אלא פשוט תחזיר מחרוזת ריקה.
     */
    @Test
    public void testPrintNullSnapshotReturnsEmptyString() {
        assertEquals("", BoardPrinter.print(null));
    }
}
