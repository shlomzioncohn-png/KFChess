import io.BoardParser;
import models.Board;
import models.Position;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class KnightRuleTest {

    @Test
    public void testAllKnightPossibilities() {
        String boardStr =
                ". . . . .\n" +
                        ". wP wP wP .\n" +
                        ". wP wN wP .\n" +
                        ". wP wP wP .\n" +
                        ". . . . .";

        Board board = BoardParser.parse(boardStr);
        Position knightPos = new Position(2, 2);

        // 1. תנועת L חוקית
        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(4, 3)));
        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(0, 1)));

        // 2. תנועה לא חוקית
        assertFalse(RuleEngine.validateMove(board, knightPos, new Position(4, 4)));
    }
}
