package rules;

import models.Board;
import models.Piece;
import models.Position;
import models.enums.PieceColor;

/**
 *  החוקים של הרגלי דורשים לדעת את צבע הכלי (כי לבן עולה למעלה ושחור יורד למטה) ואת שורת ההתחלה שלו.
 *  משתמשים במתודות של ה-Board וה-Piece כדי שהכל יחושב דינמית ללא ערכים קשיחים.
 */
public class PawnRule implements PieceRule {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece pawn = board.getPieceAt(from);
        if (pawn == null) return false;

        int rowDiff = to.getRow() - from.getRow();
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        // קביעת כיוון התנועה המותר
        int allowedSingleRowDiff = (pawn.getColor() == PieceColor.WHITE) ? -1 : 1;
        int allowedDoubleRowDiff = (pawn.getColor() == PieceColor.WHITE) ? -2 : 2;

        int startRow = (pawn.getColor() == PieceColor.WHITE) ? board.getHeight() - 1 : 0;
        // מקרה א': תנועה ישרה קדימה (אין החלפת עמודה)
        if (deltaCol == 0) {
            if (rowDiff == allowedSingleRowDiff) {
                return board.getPieceAt(to) == null;
            }
            if (from.getRow() == startRow && rowDiff == allowedDoubleRowDiff) {
                return board.getPieceAt(to) == null && BoardNavigator.isPathClear(board, from, to);
            }
            return false;
        }
        // מקרה ב': תנועה באלכסון
        if (deltaCol == 1 && rowDiff == allowedSingleRowDiff) {
            Piece target = board.getPieceAt(to);
            return target != null;
        }
        return false;
    }

}
