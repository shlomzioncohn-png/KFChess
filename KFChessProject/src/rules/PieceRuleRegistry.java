package rules;

import models.enums.PieceType;

import java.util.HashMap;
import java.util.Map;

public class PieceRuleRegistry {

    private static final Map<PieceType, PieceRule> registry = new HashMap<>();

    static {
        registry.put(PieceType.KING, new KingRule());
        registry.put(PieceType.ROOK, new RookRule());
        registry.put(PieceType.BISHOP, new BishopRule());
        registry.put(PieceType.QUEEN, new QueenRule());
        registry.put(PieceType.KNIGHT, new KnightRule());
        registry.put(PieceType.PAWN, new PawnRule());
    }

    // חסימת אפשרות ליצירת מופע של ה-Registry
    private PieceRuleRegistry() {}


    public static PieceRule getRule(PieceType type) {
        return registry.get(type);
    }
}
