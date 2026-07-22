package input;

import models.Board;
import models.Position;

public class BoardMapper {

    /**
     * ממיר קואורדינטות פיקסל (x, y) למיקום לוגי על הלוח (Row, Col).
     * cellSize חייב להיות הגודל הנוכחי שבו הלוח מצויר בפועל (דינמי, לא קבוע) -
     * אחרת קליקים יתורגמו לפי גודל שגוי אחרי resize.
     */
    public static Position mapPixelToPosition(int x, int y, Board board, int cellSize) {
        int col = x / cellSize;
        int row = y / cellSize;

        Position potentialPosition = new Position(row, col);

        if (board != null && board.isValidPosition(potentialPosition)) {
            return potentialPosition;
        }

        return null;
    }
}
