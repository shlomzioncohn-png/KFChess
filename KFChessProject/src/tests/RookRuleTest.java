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

public class RookRuleTest {

    private Board boardWithRook(Position rookPos) {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(rookPos, new Piece("r1", PieceColor.WHITE, PieceType.ROOK, rookPos));
        return board;
    }

    @Test
    void rookMovesFreelyHorizontallyAndVertically() {
        Position rookPos = new Position(4, 4);
        Board board = boardWithRook(rookPos);

        assertTrue(RuleEngine.validateMove(board, rookPos, new Position(4, 7)), "צריח חייב לזוז חופשי לאורך השורה");
        assertTrue(RuleEngine.validateMove(board, rookPos, new Position(0, 4)), "צריח חייב לזוז חופשי לאורך העמודה");
    }

    @Test
    void rookCannotMoveDiagonally() {
        Position rookPos = new Position(4, 4);
        Board board = boardWithRook(rookPos);

        assertFalse(RuleEngine.validateMove(board, rookPos, new Position(6, 6)), "צריח אינו יכול לזוז באלכסון");
    }

    @Test
    void rookIsBlockedByPieceInPath() {
        Position rookPos = new Position(4, 4);
        Board board = boardWithRook(rookPos);
        board.addPiece(new Position(4, 6), new Piece("blocker", PieceColor.WHITE, PieceType.PAWN, new Position(4, 6)));

        assertFalse(RuleEngine.validateMove(board, rookPos, new Position(4, 7)),
                "מהלך צריח דרך משבצת תפוסה חייב להיות לא חוקי");
    }

    @Test
    void rookCanCaptureEnemyAtLineEnd() {
        Position rookPos = new Position(4, 4);
        Board board = boardWithRook(rookPos);
        Position enemyPos = new Position(4, 7);
        board.addPiece(enemyPos, new Piece("enemy", PieceColor.BLACK, PieceType.PAWN, enemyPos));

        assertTrue(RuleEngine.validateMove(board, rookPos, enemyPos), "צריח חייב להיות מסוגל לתפוס כלי יריב בסוף הקו");
    }

    @Test
    void rookCannotCaptureOwnColor() {
        Position rookPos = new Position(4, 4);
        Board board = boardWithRook(rookPos);
        Position friendPos = new Position(4, 7);
        board.addPiece(friendPos, new Piece("friend", PieceColor.WHITE, PieceType.PAWN, friendPos));

        assertFalse(RuleEngine.validateMove(board, rookPos, friendPos), "צריח אינו יכול לתפוס כלי מאותו הצבע");
    }

    @Test
    void rookAtCornerCanReachOppositeEdgesOfBoard() {
        Position rookPos = new Position(0, 0);
        Board board = boardWithRook(rookPos);

        assertTrue(RuleEngine.validateMove(board, rookPos, new Position(0, 7)), "צריח בפינה חייב להגיע לקצה השורה הפנוי");
        assertTrue(RuleEngine.validateMove(board, rookPos, new Position(7, 0)), "צריח בפינה חייב להגיע לקצה העמודה הפנוי");
    }

    @Test
    void rookCannotMoveToPositionOutsideBoardBounds() {
        Position rookPos = new Position(4, 4);
        Board board = boardWithRook(rookPos);

        assertFalse(RuleEngine.validateMove(board, rookPos, new Position(4, 8)),
                "יעד מחוץ לגבולות הלוח חייב להיפסל גם כשהכיוון עצמו תקין לצריח");
    }
}
