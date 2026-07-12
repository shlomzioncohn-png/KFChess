package input;

import models.Position;

public class BoardMapper {
    public static final int CELL_SIZE = 100;

    public static final int BOARD_SIZE = 8;

    /**
     * ממיר קואורדינטות פיקסל (x, y) למיקום לוגי על הלוח (Row, Col).
     */
    public Position mapPixelToPosition(int x, int y) {
        int col = x / CELL_SIZE;
        int row = y / CELL_SIZE;

        if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
            return new Position(row, col);
        }

        return null;
    }
}
