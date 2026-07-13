package rules;

import models.Piece;
import models.enums.PieceType;

/**
 * יודעת רק דבר אחד: מתי תפיסת כלי מסיימת את המשחק.
 */
public class WinCondition {

    private WinCondition() {}

    public static boolean isDecisive(Piece capturedPiece) {
        return capturedPiece != null && capturedPiece.getType() == PieceType.KING;
    }
}
