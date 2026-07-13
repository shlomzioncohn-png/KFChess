import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class QueenRuleTest {

    private Board boardWithQueen(Position pos) {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(pos, new Piece("q1", PieceColor.WHITE, PieceType.QUEEN, pos));
        return board;
    }

    @Test
    void queenMovesStraight() {
        Position queenPos = new Position(4, 4);
        Board board = boardWithQueen(queenPos);

        assertTrue(RuleEngine.validateMove(board, queenPos, new Position(2, 4)), "מלכה חייבת לזוז חופשי בקו ישר אנכי");
        assertTrue(RuleEngine.validateMove(board, queenPos, new Position(4, 7)), "מלכה חייבת לזוז חופשי בקו ישר אופקי");
    }

    @Test
    void queenMovesDiagonally() {
        Position queenPos = new Position(4, 4);
        Board board = boardWithQueen(queenPos);

        assertTrue(RuleEngine.validateMove(board, queenPos, new Position(6, 6)), "מלכה חייבת לזוז חופשי באלכסון");
    }

    @Test
    void queenCannotMoveInLShape() {
        Position queenPos = new Position(4, 4);
        Board board = boardWithQueen(queenPos);

        assertFalse(RuleEngine.validateMove(board, queenPos, new Position(6, 5)), "מלכה אינה יכולה לזוז בצורת L כמו פרש");
    }

    @Test
    void queenIsBlockedByPieceInStraightPath() {
        Position queenPos = new Position(4, 4);
        Board board = boardWithQueen(queenPos);
        board.addPiece(new Position(4, 5), new Piece("blocker", PieceColor.WHITE, PieceType.PAWN, new Position(4, 5)));

        assertFalse(RuleEngine.validateMove(board, queenPos, new Position(4, 6)), "מלכה חסומה בקו ישר אינה יכולה לעבור דרך הכלי");
    }

    @Test
    void queenIsBlockedByPieceInDiagonalPath() {
        Position queenPos = new Position(4, 4);
        Board board = boardWithQueen(queenPos);
        board.addPiece(new Position(5, 5), new Piece("blocker", PieceColor.WHITE, PieceType.PAWN, new Position(5, 5)));

        assertFalse(RuleEngine.validateMove(board, queenPos, new Position(6, 6)), "מלכה חסומה באלכסון אינה יכולה לעבור דרך הכלי");
    }
}
