package realtime;

import engine.MoveResult;
import models.Board;
import models.Position;
import rules.RuleEngine;

/**
 * אחראי על בדיקת חוקיות מהלך (בהתבסס על חוקי השחמט)
 * ועל חישוב הזמן שייקח לכלי להגיע ליעדו.
 */
public class RealTimeArbiter {
    private static final long TIME_PER_TILE = 1000;

    public MoveResult validateMove(Board board, Position src, Position dest) {
        boolean isLegal = RuleEngine.validateMove(board, src, dest);

        long travelTime = calculateTravelTime(src, dest);

        if (isLegal) {
            return new MoveResult(true, src, dest, "Valid move", travelTime);
        } else {
            return new MoveResult(false, src, dest, "Illegal move", 0);
        }
    }

    public long calculateTravelTime(Position src, Position dest) {
        int rowDiff = Math.abs(src.getRow() - dest.getRow());
        int colDiff = Math.abs(src.getCol() - dest.getCol());

        int distance = Math.max(rowDiff, colDiff);

        return distance * TIME_PER_TILE;
    }

}
