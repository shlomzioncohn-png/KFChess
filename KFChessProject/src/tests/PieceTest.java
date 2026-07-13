import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceState;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PieceTest {
    @Test
    public void testPieceStateChanges() {
        Piece p = new Piece("p1", PieceColor.WHITE, PieceType.PAWN, new Position(0,0));
        assertEquals(PieceState.IDLE, p.getState());

        // When: מעבירים אותו ל-MOVING
        p.setState(PieceState.MOVING);
        assertEquals(PieceState.MOVING, p.getState());

        // When: מעבירים אותו ל-AIRBORNE (זה הפיצ'ר החדש!)
        p.setState(PieceState.AIRBORNE);
        assertEquals(PieceState.AIRBORNE, p.getState());

        p.setState(PieceState.CAPTURED);
        assertEquals(PieceState.CAPTURED, p.getState());
    }
}
