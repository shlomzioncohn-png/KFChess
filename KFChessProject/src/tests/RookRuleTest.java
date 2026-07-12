import io.BoardParser;
import models.Board;
import models.Position;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;
public class RookRuleTest {
    @Test
    public void testAllRookPossibilities() {
        String boardStr =
                ". . . .\n" +
                        ". . . .\n" +
                        "wP . . .\n" +
                        ". . . .\n" +
                        "wR . . bP";

        Board board = BoardParser.parse(boardStr);
        Position rookPos = new Position(4, 0);

        // 1. תנועה חוקית אופקית במסלול פנוי
        assertTrue(RuleEngine.validateMove(board, rookPos, new Position(4, 2)));

        // 2. אכילת אויב בסוף המסלול (חוקי)
        assertTrue(RuleEngine.validateMove(board, rookPos, new Position(4, 3)));

        // 3. תנועה חסומה אנכית
        assertFalse(RuleEngine.validateMove(board, rookPos, new Position(0, 0)));

        // 4. תנועה באלכסון (לא חוקי לצריח)
        assertFalse(RuleEngine.validateMove(board, rookPos, new Position(3, 1)));
    }
}
