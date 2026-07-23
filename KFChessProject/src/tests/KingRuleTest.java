package  tests;

import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class KingRuleTest {

    private Board boardWithKing(Position pos) {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(pos, new Piece("k1", PieceColor.WHITE, PieceType.KING, pos));
        return board;
    }

    @Test
    void kingMovesOneStepInAnyDirection() {
        Position kingPos = new Position(4, 4);
        Board board = boardWithKing(kingPos);

        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(3, 4)), "מלך יכול לזוז צעד אחד למעלה");
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(5, 4)), "מלך יכול לזוז צעד אחד למטה");
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(4, 3)), "מלך יכול לזוז צעד אחד שמאלה");
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(3, 3)), "מלך יכול לזוז צעד אחד באלכסון");
    }

    @Test
    void kingCannotMoveMoreThanOneStep() {
        Position kingPos = new Position(4, 4);
        Board board = boardWithKing(kingPos);

        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(4, 2)), "מלך אינו יכול לזוז 2 צעדים שמאלה");
        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(2, 4)), "מלך אינו יכול לזוז 2 צעדים למעלה");
        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(6, 6)), "מלך אינו יכול לזוז 2 צעדים באלכסון");
    }

    @Test
    void kingCannotMoveInLShape() {
        Position kingPos = new Position(4, 4);
        Board board = boardWithKing(kingPos);

        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(6, 5)), "מלך אינו יכול לזוז בצורת L כמו פרש");
    }

    @Test
    void kingCannotCaptureOwnColor() {
        Position kingPos = new Position(4, 4);
        Board board = boardWithKing(kingPos);
        Position friendPos = new Position(4, 3);
        board.addPiece(friendPos, new Piece("friend", PieceColor.WHITE, PieceType.PAWN, friendPos));

        assertFalse(RuleEngine.validateMove(board, kingPos, friendPos), "מלך אינו יכול לתפוס כלי מאותו הצבע");
    }

    @Test
    void kingAtCornerHasOnlyTheThreeAdjacentMovesThatStayOnBoard() {
        Position kingPos = new Position(0, 0);
        Board board = boardWithKing(kingPos);

        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(0, 1)));
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(1, 0)));
        assertTrue(RuleEngine.validateMove(board, kingPos, new Position(1, 1)));
        assertFalse(RuleEngine.validateMove(board, kingPos, new Position(-1, 0)),
                "צעד שהיה חוקי בגודלו אך יוצא מגבולות הלוח חייב להיפסל");
    }
}
