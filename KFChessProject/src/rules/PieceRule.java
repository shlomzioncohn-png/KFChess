package rules;

import models.Board;
import models.Position;

/**
 * ממשק המייצג כלל תנועה מופשט עבור כלי שחמט בודד.
 * כל כלי יממש את הלוגיקה הייחודית לו.
 */

public interface PieceRule {
    boolean isValidMove(Board board, Position from, Position to);
}
