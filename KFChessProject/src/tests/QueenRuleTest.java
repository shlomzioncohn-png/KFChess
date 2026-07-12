import io.BoardParser;
import models.Board;
import models.Position;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class QueenRuleTest {
    @Test
    public void testQueenAllPossibilities() {
        String boardStr =
                ". . wP . .\n" +
                        ". . . . .\n" +
                        ". . wQ . .\n" +
                        ". . . . .\n" +
                        ". . . . .";

        Board board = BoardParser.parse(boardStr);
        Position queenPos = new Position(2, 2);

        // 1. תנועה חוקית כמו צריח
        assertTrue(RuleEngine.validateMove(board, queenPos, new Position(2, 4)));

        // 2. תנועה חוקית כמו רץ
        assertTrue(RuleEngine.validateMove(board, queenPos, new Position(4, 4)));

        // 3. תנועה חסומה
        assertFalse(RuleEngine.validateMove(board, queenPos, new Position(0, 2)));

        // 4. תנועה לא חוקית לחלוטין
        assertFalse(RuleEngine.validateMove(board, queenPos, new Position(4, 3)));
    }
}
