import io.BoardParser;
import models.Board;
import models.Position;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;
public class BishopRuleTest {
    @Test
    public void testAllBishopPossibilities() {
        String boardStr =
                "bP . . . .\n" +
                        ". . . . .\n" +
                        ". . wB . .\n" +
                        ". . . . .\n" +
                        ". . . . .";

        Board board = BoardParser.parse(boardStr);
        Position bishopPos = new Position(2, 2);

        // 1. תנועה חוקית באלכסון פנוי
        assertTrue(RuleEngine.validateMove(board, bishopPos, new Position(4, 4)));
        assertTrue(RuleEngine.validateMove(board, bishopPos, new Position(4, 0)));

        // 2. תנועה לא חוקית (ישרה)
        assertFalse(RuleEngine.validateMove(board, bishopPos, new Position(2, 4)));

        //  חסימה אלכסונית
        String blockedBoardStr =
                ". . . .\n" +
                        ". wP . .\n" +
                        ". . wB .\n" +
                        ". . . .";
        Board blockedBoard = BoardParser.parse(blockedBoardStr);
        assertFalse(RuleEngine.validateMove(blockedBoard, new Position(2, 2), new Position(0, 0)));
    }
}
