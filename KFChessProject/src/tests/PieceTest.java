import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceState;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PieceTest {

    @Test
    void newPieceStartsIdleWithConstructorValues() {
        Position start = new Position(0, 0);
        Piece p = new Piece("p1", PieceColor.WHITE, PieceType.PAWN, start);

        assertEquals("p1", p.getId(), "המזהה חייב להיות זהה לזה שהוזן בבנאי");
        assertEquals(PieceColor.WHITE, p.getColor(), "הצבע חייב להיות זהה לזה שהוזן בבנאי");
        assertEquals(PieceType.PAWN, p.getType(), "סוג הכלי חייב להיות זהה לזה שהוזן בבנאי");
        assertEquals(start, p.getCell(), "המיקום ההתחלתי חייב להיות זהה לזה שהוזן בבנאי");
        assertEquals(PieceState.IDLE, p.getState(), "כלי חדש חייב להתחיל במצב IDLE");
    }

    @Test
    void stateTransitionsAreStoredCorrectly() {
        Piece p = new Piece("p2", PieceColor.BLACK, PieceType.KNIGHT, new Position(1, 1));

        assertEquals(PieceState.IDLE, p.getState());

        p.setState(PieceState.MOVING);
        assertEquals(PieceState.MOVING, p.getState());

        p.setState(PieceState.AIRBORNE);
        assertEquals(PieceState.AIRBORNE, p.getState());

        p.setState(PieceState.CAPTURED);
        assertEquals(PieceState.CAPTURED, p.getState());
    }

    @Test
    void setCellUpdatesPosition() {
        Piece p = new Piece("p3", PieceColor.WHITE, PieceType.ROOK, new Position(0, 0));
        Position dest = new Position(3, 3);
        p.setCell(dest);
        assertEquals(dest, p.getCell(), "לאחר setCell, getCell חייב להחזיר את המיקום החדש");
    }

    @Test
    void promoteChangesTypeButKeepsOtherFields() {
        Piece p = new Piece("p4", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0));
        p.promote(PieceType.QUEEN);
        assertEquals(PieceType.QUEEN, p.getType(), "לאחר הכתרה, סוג הכלי חייב להשתנות למלכה");
        assertEquals("p4", p.getId(), "המזהה אינו אמור להשתנות בעקבות הכתרה");
        assertEquals(PieceColor.WHITE, p.getColor(), "הצבע אינו אמור להשתנות בעקבות הכתרה");
    }

    @Test
    void isProtectedByJumpTrueWhileJumpingAndBeforeExpiry() {
        Piece p = new Piece("p5", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0));
        p.setState(PieceState.JUMPING);
        p.setJumpExpiryTime(1000L);

        assertTrue(p.isProtectedByJump(500L), "בזמן שהכלי במצב JUMPING ולפני זמן התפוגה, הוא אמור להיות מוגן");
    }

    @Test
    void isProtectedByJumpFalseAfterExpiry() {
        Piece p = new Piece("p6", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0));
        p.setState(PieceState.JUMPING);
        p.setJumpExpiryTime(1000L);

        assertFalse(p.isProtectedByJump(1000L), "בדיוק בזמן התפוגה (או אחריו) הכלי אינו אמור להיות מוגן");
        assertFalse(p.isProtectedByJump(1500L), "אחרי זמן התפוגה הכלי אינו אמור להיות מוגן");
    }

    @Test
    void isProtectedByJumpFalseWhenNotJumping() {
        Piece p = new Piece("p7", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0));
        p.setJumpExpiryTime(1000L);
        assertFalse(p.isProtectedByJump(500L), "כלי שאינו במצב JUMPING אינו אמור להיות מוגן, גם אם הוגדר לו זמן תפוגה");
    }
}
