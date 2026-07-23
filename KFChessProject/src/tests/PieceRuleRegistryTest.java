package  tests;

import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.*;

import static org.junit.jupiter.api.Assertions.*;

public class PieceRuleRegistryTest {

    @Test
    void returnsCorrectRuleForEachPieceType() {
        assertInstanceOf(KingRule.class, PieceRuleRegistry.getRule(PieceType.KING));
        assertInstanceOf(RookRule.class, PieceRuleRegistry.getRule(PieceType.ROOK));
        assertInstanceOf(BishopRule.class, PieceRuleRegistry.getRule(PieceType.BISHOP));
        assertInstanceOf(QueenRule.class, PieceRuleRegistry.getRule(PieceType.QUEEN));
        assertInstanceOf(KnightRule.class, PieceRuleRegistry.getRule(PieceType.KNIGHT));
        assertInstanceOf(PawnRule.class, PieceRuleRegistry.getRule(PieceType.PAWN));
    }

    @Test
    void sameRuleInstanceIsReturnedOnMultipleCalls() {
        PieceRule first = PieceRuleRegistry.getRule(PieceType.ROOK);
        PieceRule second = PieceRuleRegistry.getRule(PieceType.ROOK);
        assertSame(first, second, "ה-Registry חייב להחזיר את אותו מופע של הכלל בכל קריאה");
    }
}
