package rules;

import models.Board;
import models.Piece;
import models.Position;


public class RuleEngine {

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

        PieceRule rule = PieceRuleRegistry.getRule(movingPiece.getType());
        if (rule == null) return false;

        return rule.isValidMove(board, from, to);
    }
}
