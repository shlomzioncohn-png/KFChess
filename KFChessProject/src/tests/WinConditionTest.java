import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.WinCondition;

import static org.junit.jupiter.api.Assertions.*;

public class WinConditionTest {

    @Test
    void capturingKingIsDecisive() {
        Piece king = new Piece("k", PieceColor.BLACK, PieceType.KING, new Position(0, 0));
        assertTrue(WinCondition.isDecisive(king), "תפיסת מלך חייבת להיחשב מכרעת ולסיים את המשחק");
    }

    @Test
    void capturingNonKingIsNotDecisive() {
        Piece rook = new Piece("r", PieceColor.BLACK, PieceType.ROOK, new Position(0, 0));
        assertFalse(WinCondition.isDecisive(rook), "תפיסת כלי שאינו מלך אינה אמורה לסיים את המשחק");
    }

    @Test
    void nullPieceIsNotDecisive() {
        assertFalse(WinCondition.isDecisive(null), "תפיסת null אינה אמורה לגרום לקריסה ואינה אמורה להיחשב מכרעת");
    }
}
