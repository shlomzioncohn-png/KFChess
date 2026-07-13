import engine.MoveResult;
import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import realtime.RealTimeArbiter;

import static org.junit.jupiter.api.Assertions.*;

public class RealTimeArbiterTest {

    @Test
    void legalMoveProducesSuccessfulResult() {
        Board board = new MatrixBoard(8, 8);
        Position src = new Position(4, 4);
        board.addPiece(src, new Piece("r", PieceColor.WHITE, PieceType.ROOK, src));
        RealTimeArbiter arbiter = new RealTimeArbiter();

        MoveResult result = arbiter.validateMove(board, src, new Position(4, 7));

        assertTrue(result.isSuccess(), "מהלך חוקי חייב להחזיר תוצאה מוצלחת");
        assertEquals(3000L, result.getTravelTime(), "זמן הנסיעה חייב להיות מרחק כפול 1000 מילישניות");
    }

    @Test
    void illegalMoveProducesFailedResultWithZeroTravelTime() {
        Board board = new MatrixBoard(8, 8);
        Position src = new Position(4, 4);
        board.addPiece(src, new Piece("r", PieceColor.WHITE, PieceType.ROOK, src));
        RealTimeArbiter arbiter = new RealTimeArbiter();

        MoveResult result = arbiter.validateMove(board, src, new Position(6, 6));

        assertFalse(result.isSuccess(), "מהלך לא חוקי חייב להחזיר תוצאה שאינה מוצלחת");
        assertEquals(0L, result.getTravelTime(), "מהלך לא חוקי לא אמור לצרוך זמן נסיעה");
    }

    @Test
    void travelTimeUsesDiagonalDistanceWhenGreater() {
        RealTimeArbiter arbiter = new RealTimeArbiter();
        long time = arbiter.calculateTravelTime(new Position(0, 0), new Position(3, 5));
        assertEquals(5000L, time, "זמן הנסיעה חייב להתבסס על המרחק הגדול מבין השורות והעמודות");
    }

    @Test
    void travelTimeIsZeroForSamePosition() {
        RealTimeArbiter arbiter = new RealTimeArbiter();
        long time = arbiter.calculateTravelTime(new Position(2, 2), new Position(2, 2));
        assertEquals(0L, time, "מרחק אפס חייב להניב זמן נסיעה אפס");
    }
}
