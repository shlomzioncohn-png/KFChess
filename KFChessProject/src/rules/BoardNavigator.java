package rules;


import models.Board;
import models.Position;

/**
 * מחלקת שירות פנימית לשכבת הכללים בלבד.
 * אחראית על מתמטיקה וסריקת מסלולים על גבי הלוח.
 */
public class BoardNavigator {

    private BoardNavigator() {} // חסימת אפשרות ליצירת מופע

    public static boolean isPathClear(Board board, Position from, Position to) {
        int stepRow = Integer.compare(to.getRow(), from.getRow());
        int stepCol = Integer.compare(to.getCol(), from.getCol());

        int currentRow = from.getRow() + stepRow;
        int currentCol = from.getCol() + stepCol;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (board.getPieceAt(new Position(currentRow, currentCol)) != null) {
                return false;
            }
            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }

}
