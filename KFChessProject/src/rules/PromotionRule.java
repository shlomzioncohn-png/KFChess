package rules;

import models.Board;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;

/**
 * יודעת רק דבר אחד: מתי כלי זכאי להכתרה, ולאיזה סוג.
 * לא יודעת דבר על תזמון, לכידה, או מצב המשחק הכללי.
 */
public class PromotionRule {

    private PromotionRule() {}

    public static boolean isEligible(Board board, Piece piece, Position dest) {
        if (piece.getType() != PieceType
                .PAWN) {
            return false;
        }
        int lastRow = (piece.getColor() == PieceColor.WHITE) ? 0 : board.getHeight() - 1;
        return dest.getRow() == lastRow;
    }

    public static PieceType promotedType() {
        return PieceType.QUEEN;
    }
}
