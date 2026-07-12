package rules;

import models.Board;
import models.Piece;
import models.Position;
import models.enums.PieceType;

import java.util.HashMap;
import java.util.Map;

public class RuleEngine {

    private static final Map<PieceType, PieceRule> rules = new HashMap<>();

    static {
        rules.put(PieceType.KING, new KingRule());
        rules.put(PieceType.ROOK, new RookRule());
        rules.put(PieceType.BISHOP, new BishopRule());
        rules.put(PieceType.QUEEN, new QueenRule());
        rules.put(PieceType.KNIGHT, new KnightRule());
        rules.put(PieceType.PAWN, new PawnRule());
    }

    /**
     * הפונקציה המרכזית והיחידה שמנוע המשחק או הקונטרולר קוראים לה כדי לאמת מהלך.
     */
    public static boolean validateMove(Board board, Position from, Position to) {
        if (board == null || from == null || to == null) return false;
        if (!board.isValidPosition(from) || !board.isValidPosition(to)) return false;

        if (from.getRow() == to.getRow() && from.getCol() == to.getCol()) return false;

        Piece movingPiece = board.getPieceAt(from);
        if (movingPiece == null) return false;

        Piece targetPiece = board.getPieceAt(to);
        if (targetPiece != null && targetPiece.getColor() == movingPiece.getColor()) {
            return false;
        }
        PieceRule rule = rules.get(movingPiece.getType());
        if (rule == null) return false;
        return rule.isValidMove(board, from, to);
    }
}
