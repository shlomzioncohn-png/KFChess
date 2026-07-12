package rules;

import models.Board;
import models.Position;

/**
 *   צריח-הכלי שנמצא בפינות הלוח וזז רק בקווים ישרים – אופקית ואנכית
 */
public class RookRule implements PieceRule {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        boolean isStraight = (deltaRow == 0 && deltaCol > 0) || (deltaCol == 0 && deltaRow > 0);
        if (!isStraight) return false;

        return BoardNavigator.isPathClear(board, from, to);
    }
}
