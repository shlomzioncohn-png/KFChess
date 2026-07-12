import io.BoardParser;
import models.Board;
import models.Position;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class PawnRuleTest {
    @Test
    public void testWhitePawnPossibilities() {
        String boardStr =
                ". . . .\n" +
                        ". . bP bP\n" +
                        ". . wP .\n" +
                        ". . . .";
        Board board = BoardParser.parse(boardStr);
        Position pawnPos = new Position(2, 2);

        // 1. חסום ישר קדימה
        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(1, 2)));

        // 2. אכילה באלכסון
        assertTrue(RuleEngine.validateMove(board, pawnPos, new Position(1, 3)));

        // 3. תנועה באלכסון למשבצת ריקה (לא חוקי!)
        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(1, 1)));
    }

    @Test
    public void testPawnDoubleMoveAndBlock() {
        // לוח שבו חייל לבן בשורת המקור מנסה צעד כפול, אבל משבצת האמצע חסומה
        String boardStr =
                ". . . .\n" +
                        ". . . .\n" +
                        ". . bP .\n" +
                        ". . wP .\n" +
                        ". . . .";
        Board board = BoardParser.parse(boardStr);

        assertFalse(RuleEngine.validateMove(board, new Position(3, 2), new Position(1, 2)));
    }
}
