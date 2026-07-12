package rules;

import models.Board;
import models.Position;

/**
 *רץ- זז באלכסונים לכל מרחק, בתנאי שהדרך פנויה.
 */
public class BishopRule implements  PieceRule
{

        @Override
        public boolean isValidMove(Board board, Position from, Position to) {
            int deltaRow = Math.abs(to.getRow() - from.getRow());
            int deltaCol = Math.abs(to.getCol() - from.getCol());

            if (deltaRow != deltaCol) {
                return false;
            }
            return BoardNavigator.isPathClear(board, from, to);
        }
}
