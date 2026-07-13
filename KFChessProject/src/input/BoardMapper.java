package input;

import models.Board;
import models.Position;

public class BoardMapper {
    public static final int CELL_SIZE = 100;


    /**
     * ממיר קואורדינטות פיקסל (x, y) למיקום לוגי על הלוח (Row, Col).
     */
    public static Position mapPixelToPosition(int x, int y, Board board) {
        int col = x / CELL_SIZE;
        int row = y / CELL_SIZE;

        Position potentialPosition = new Position(row, col);

        if (board != null && board.isValidPosition(potentialPosition)) {
            return potentialPosition;
        }

        return null;
    }
}
