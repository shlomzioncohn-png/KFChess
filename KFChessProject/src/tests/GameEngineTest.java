import engine.GameEngine;
import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {

    @Test
    public void testMovePieceUpdatesCorrectly() {
        MatrixBoard board = new MatrixBoard(8, 8);
        Position p1 = new Position(0, 0);
        Position p2 = new Position(0, 1);

        Piece p = new Piece("p1", PieceColor.WHITE, PieceType.PAWN, p1);
        board.addPiece(p1, p);

        board.movePiece(p1, p2);

        assertNull(board.getPieceAt(p1), "התא המקורי חייב להיות ריק");
        assertEquals(p, board.getPieceAt(p2), "הכלי חייב לעבור ליעד");
        assertEquals(p2, p.getCell(), "הכלי חייב לעדכן את המיקום הפנימי שלו");
    }

    @Test
    public void testDoubleOccupancyThrowsException() {
        MatrixBoard board = new MatrixBoard(8, 8);
        Position p1 = new Position(0, 0);

        board.addPiece(p1, new Piece("p1", PieceColor.WHITE, PieceType.PAWN, p1));

        assertThrows(IllegalStateException.class, () -> {
            board.addPiece(p1, new Piece("p2", PieceColor.BLACK, PieceType.ROOK, p1));
        }, "אסור להוסיף כלי למשבצת תפוסה");
    }
}
