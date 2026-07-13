package tests;
import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.BoardNavigator;

import static org.junit.jupiter.api.Assertions.*;

public class BoardNavigatorTest {

    @Test
    void pathIsClearWhenNoPiecesBetween() {
        Board board = new MatrixBoard(8, 8);
        assertTrue(BoardNavigator.isPathClear(board, new Position(0, 0), new Position(0, 5)),
                "כאשר אין כלים בדרך, המסלול חייב להיחשב פנוי");
    }

    @Test
    void pathIsBlockedByPieceOnHorizontalLine() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 3), new Piece("b", PieceColor.WHITE, PieceType.PAWN, new Position(0, 3)));

        assertFalse(BoardNavigator.isPathClear(board, new Position(0, 0), new Position(0, 5)),
                "כלי שנמצא בדרך האופקית חייב לחסום את המסלול");
    }

    @Test
    void pathIsBlockedByPieceOnVerticalLine() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(3, 0), new Piece("b", PieceColor.WHITE, PieceType.PAWN, new Position(3, 0)));

        assertFalse(BoardNavigator.isPathClear(board, new Position(0, 0), new Position(5, 0)),
                "כלי שנמצא בדרך האנכית חייב לחסום את המסלול");
    }

    @Test
    void pathIsBlockedByPieceOnDiagonalLine() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(3, 3), new Piece("b", PieceColor.WHITE, PieceType.PAWN, new Position(3, 3)));

        assertFalse(BoardNavigator.isPathClear(board, new Position(0, 0), new Position(5, 5)),
                "כלי שנמצא בדרך האלכסונית חייב לחסום את המסלול");
    }

    @Test
    void adjacentCellsAreAlwaysClear() {
        Board board = new MatrixBoard(8, 8);
        assertTrue(BoardNavigator.isPathClear(board, new Position(0, 0), new Position(0, 1)),
                "בין שתי משבצות סמוכות אין תאים ביניים, לכן המסלול תמיד פנוי");
    }

    @Test
    void pieceExactlyAtDestinationDoesNotBlockPath() {
        Board board = new MatrixBoard(8, 8);
        Position dest = new Position(0, 5);
        board.addPiece(dest, new Piece("target", PieceColor.BLACK, PieceType.PAWN, dest));

        assertTrue(BoardNavigator.isPathClear(board, new Position(0, 0), dest),
                "כלי שנמצא במשבצת היעד עצמה אינו אמור להיחשב כחוסם את המסלול");
    }
}
