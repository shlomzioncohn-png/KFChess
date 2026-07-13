package rules;

import models.Board;
import models.Piece;
import models.Position;
import models.enums.PieceColor;

/**
 *  החוקים של הרגלי דורשים לדעת את צבע הכלי ואת שורת ההתחלה שלו.
 */
public class PawnRule implements PieceRule {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece pawn = board.getPieceAt(from);
        if (pawn == null) return false;

        int rowDiff = to.getRow() - from.getRow();
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        int allowedSingleRowDiff = (pawn.getColor() == PieceColor.WHITE) ? -1 : 1;
        int allowedDoubleRowDiff = (pawn.getColor() == PieceColor.WHITE) ? -2 : 2;

        int startRow = (pawn.getColor() == PieceColor.WHITE) ? board.getHeight() - 2 : 1;

        // מקרה א': תנועה ישרה קדימה
        if (deltaCol == 0) {
            if (rowDiff == allowedSingleRowDiff) {
                return board.getPieceAt(to) == null;
            }
            if (from.getRow() == startRow && rowDiff == allowedDoubleRowDiff) {
                return board.getPieceAt(to) == null && BoardNavigator.isPathClear(board, from, to);
            }
            return false;
        }

        // מקרה ב': תנועה באלכסון (לכידה)
        if (deltaCol == 1 && rowDiff == allowedSingleRowDiff) {
            Piece target = board.getPieceAt(to);
            return target != null; // לכידה חוקית רק אם יש שם כלי
        }
        return false;
    }

}
