import io.BoardParser;
import models.Board;
import models.Position;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;
import static org.junit.jupiter.api.Assertions.*;

public class KingRuleTest {

    @Test
    public void testAllKingPossibilities() {
        String boardStr =
                ". . . . .\n" +
                        ". . . . .\n" +
                        ". . wK wP .\n" +
                        ". . . . .\n" +
                        ". . . . .";

        Board board = BoardParser.parse(boardStr);
        Position kingPos = new Position(2, 2);

        // 1. תנועות חוקיות (צעד אחד לכל כיוון)
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(1, 2))); // למעלה
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(3, 2))); // למטה
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(2, 1))); // שמאלה
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(1, 3))); // אלכסון

        // 2. תנועה לא חוקית - יותר מצעד אחד
        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(2, 0))); // 2 צעדים שמאלה
        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(0, 2))); // 2 צעדים למעלה

        // 3. הכאה עצמית - מנסה לאכול את החייל הלבן שלו
        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(2, 3)));
    }
}
