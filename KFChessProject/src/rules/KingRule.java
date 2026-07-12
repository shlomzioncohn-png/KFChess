package rules;

import models.Board;
import models.Position;

/**
 *המלך זז לכל היותר משבצת אחת לכל כיוון. הוא אינו זקוק ל-BoardNavigator כי המרחק שלו הוא לכל היותר צעד אחד,
 *  הבדיקה לגבי משבצת היעד, אם יש שם חבר לקבוצה, כבר נעשית ב-RuleEngine הראשי.
 */

public class KingRule implements PieceRule{

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        return (deltaRow <= 1 && deltaCol <= 1);
    }
}
