package rules;

import models.Board;
import models.Position;

/**
 * הפרש זז בצורת L. מכיוון שהוא הכלי היחיד בשחמט שמדלג מעל כלים אחרים,
 * הוא לא משתמש ב-BoardNavigator.isPathClear. לא משנה מה קורה באמצע הדרך
 */

public class KnightRule implements PieceRule{

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        // תנועה בצורת L: או (2 שורות ו-1 עמודה) או (1 שורה ו-2 עמודות)
        return (deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2);
    }

}
