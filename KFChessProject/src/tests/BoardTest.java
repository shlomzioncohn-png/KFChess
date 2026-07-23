
package  tests;
import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void widthAndHeightMatchConstructor() {
        Board board = new MatrixBoard(4, 5);
        assertEquals(4, board.getWidth(), "הרוחב שנשמר בלוח חייב להיות 4");
        assertEquals(5, board.getHeight(), "הגובה שנשמר בלוח חייב להיות 5");
    }

    @Test
    void isValidPositionRespectsBoundaries() {
        Board board = new MatrixBoard(8, 8);
        assertTrue(board.isValidPosition(new Position(0, 0)), "הפינה השמאלית העליונה (0,0) חוקית");
        assertTrue(board.isValidPosition(new Position(7, 7)), "הפינה הימנית התחתונה (7,7) חוקית");
        assertFalse(board.isValidPosition(new Position(8, 5)), "שורה 8 לא חוקית בלוח שהשורה המקסימלית שלו היא 7");
        assertFalse(board.isValidPosition(new Position(3, -1)), "עמודה שלילית (-1) היא מחוץ לגבולות הלוח");
        assertFalse(board.isValidPosition(null), "מיקום שהוא null אינו חוקי ואסור שיקרוס");
    }

    @Test
    void newCellStartsEmpty() {
        Board board = new MatrixBoard(4, 4);
        Position pos = new Position(1, 1);
        assertNull(board.getPieceAt(pos), "תא חדש בלוח חייב להתחיל כשהוא ריק (null)");
    }

    @Test
    void getPieceAtInvalidPositionReturnsNull() {
        Board board = new MatrixBoard(4, 4);
        assertNull(board.getPieceAt(new Position(-1, 0)), "מיקום לא חוקי אמור להחזיר null ולא לקרוס");
        assertNull(board.getPieceAt(null), "מיקום null אמור להחזיר null ולא לקרוס");
    }

    @Test
    void addPieceThenGetPieceAtReturnsIt() {
        Board board = new MatrixBoard(4, 4);
        Position pos = new Position(2, 2);
        Piece whitePawn = new Piece("wp1", PieceColor.WHITE, PieceType.PAWN, pos);

        board.addPiece(pos, whitePawn);

        assertEquals(whitePawn, board.getPieceAt(pos), "הלוח צריך להחזיר את הרגלי הלבן מהמשבצת");
        assertEquals(pos, whitePawn.getCell(), "addPiece חייב לעדכן גם את המיקום הפנימי של הכלי");
    }

    @Test
    void addPieceOnOccupiedCellThrows() {
        Board board = new MatrixBoard(4, 4);
        Position pos = new Position(0, 0);
        board.addPiece(pos, new Piece("a", PieceColor.WHITE, PieceType.PAWN, pos));

        assertThrows(IllegalStateException.class, () -> {
            board.addPiece(pos, new Piece("b", PieceColor.BLACK, PieceType.PAWN, pos));
        }, "הוספת כלי למשבצת תפוסה חייבת לזרוק IllegalStateException");
    }

    @Test
    void addPieceOutOfBoundsThrows() {
        Board board = new MatrixBoard(4, 4);
        assertThrows(IllegalArgumentException.class, () -> {
            board.addPiece(new Position(9, 9), new Piece("a", PieceColor.WHITE, PieceType.PAWN, new Position(9, 9)));
        }, "הוספת כלי מחוץ לגבולות הלוח חייבת לזרוק IllegalArgumentException");
    }

    @Test
    void removePieceClearsCell() {
        Board board = new MatrixBoard(4, 4);
        Position pos = new Position(1, 2);
        board.addPiece(pos, new Piece("c", PieceColor.WHITE, PieceType.PAWN, pos));

        board.removePiece(pos);

        assertNull(board.getPieceAt(pos), "אחרי הסרת הכלי, המשבצת חייבת לחזור להיות null");
    }

    @Test
    void removePieceOnEmptyCellDoesNotThrow() {
        Board board = new MatrixBoard(4, 4);
        assertDoesNotThrow(() -> board.removePiece(new Position(0, 0)),
                "הסרת כלי ממשבצת ריקה אינה אמורה לזרוק שגיאה");
    }

    @Test
    void removePieceOutOfBoundsDoesNotThrow() {
        Board board = new MatrixBoard(4, 4);
        assertDoesNotThrow(() -> board.removePiece(new Position(-1, -1)),
                "הסרת כלי ממיקום לא חוקי אינה אמורה לזרוק שגיאה");
    }

    @Test
    void movePieceRelocatesPieceAndUpdatesItsCell() {
        Board board = new MatrixBoard(4, 4);
        Position src = new Position(0, 0);
        Position dest = new Position(2, 2);
        Piece piece = new Piece("d", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, piece);

        board.movePiece(src, dest);

        assertNull(board.getPieceAt(src), "אחרי מהלך, המשבצת המקורית חייבת להתרוקן");
        assertEquals(piece, board.getPieceAt(dest), "אחרי מהלך, הכלי חייב להימצא במשבצת היעד");
        assertEquals(dest, piece.getCell(), "אחרי מהלך, מיקום הכלי הפנימי חייב להתעדכן ליעד");
    }

    @Test
    void movePieceFromEmptyCellThrows() {
        Board board = new MatrixBoard(4, 4);
        assertThrows(IllegalStateException.class, () -> {
            board.movePiece(new Position(0, 0), new Position(1, 1));
        }, "מהלך ממשבצת ריקה חייב לזרוק IllegalStateException");
    }

    @Test
    void movePieceOutOfBoundsThrows() {
        Board board = new MatrixBoard(4, 4);
        Position src = new Position(0, 0);
        board.addPiece(src, new Piece("e", PieceColor.WHITE, PieceType.ROOK, src));

        assertThrows(IllegalArgumentException.class, () -> {
            board.movePiece(src, new Position(9, 9));
        }, "מהלך אל מחוץ לגבולות הלוח חייב לזרוק IllegalArgumentException");
    }
}
