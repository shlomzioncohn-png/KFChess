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

public class BishopRuleTest {

    private Board boardWithBishop(Position pos) {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(pos, new Piece("b1", PieceColor.WHITE, PieceType.BISHOP, pos));
        return board;
    }

    @Test
    void bishopMovesFreelyAlongDiagonal() {
        Position bishopPos = new Position(4, 4);
        Board board = boardWithBishop(bishopPos);

        assertTrue(RuleEngine.validateMove(board, bishopPos, new Position(7, 7)), "רץ חייב לזוז חופשי באלכסון");
        assertTrue(RuleEngine.validateMove(board, bishopPos, new Position(2, 2)), "רץ חייב לזוז חופשי באלכסון ההפוך");
    }

    @Test
    void bishopCannotMoveStraight() {
        Position bishopPos = new Position(4, 4);
        Board board = boardWithBishop(bishopPos);

        assertFalse(RuleEngine.validateMove(board, bishopPos, new Position(4, 7)), "רץ אינו יכול לזוז בקו ישר אופקי");
        assertFalse(RuleEngine.validateMove(board, bishopPos, new Position(0, 4)), "רץ אינו יכול לזוז בקו ישר אנכי");
    }

    @Test
    void bishopIsBlockedByPieceInDiagonalPath() {
        Position bishopPos = new Position(4, 4);
        Board board = boardWithBishop(bishopPos);
        board.addPiece(new Position(5, 5), new Piece("blocker", PieceColor.WHITE, PieceType.PAWN, new Position(5, 5)));

        assertFalse(RuleEngine.validateMove(board, bishopPos, new Position(7, 7)),
                "מהלך רץ דרך משבצת תפוסה חייב להיות לא חוקי");
    }

    @Test
    void bishopCanCaptureEnemyAtDiagonalEnd() {
        Position bishopPos = new Position(4, 4);
        Board board = boardWithBishop(bishopPos);
        Position enemyPos = new Position(6, 6);
        board.addPiece(enemyPos, new Piece("enemy", PieceColor.BLACK, PieceType.PAWN, enemyPos));

        assertTrue(RuleEngine.validateMove(board, bishopPos, enemyPos), "רץ חייב להיות מסוגל לתפוס כלי יריב בסוף האלכסון");
    }

    @Test
    void bishopCannotCaptureOwnColor() {
        Position bishopPos = new Position(4, 4);
        Board board = boardWithBishop(bishopPos);
        Position friendPos = new Position(6, 6);
        board.addPiece(friendPos, new Piece("friend", PieceColor.WHITE, PieceType.PAWN, friendPos));

        assertFalse(RuleEngine.validateMove(board, bishopPos, friendPos), "רץ אינו יכול לתפוס כלי מאותו הצבע");
    }

    @Test
    void bishopAtCornerCanReachFarCornerAlongDiagonal() {
        Position bishopPos = new Position(0, 0);
        Board board = boardWithBishop(bishopPos);

        assertTrue(RuleEngine.validateMove(board, bishopPos, new Position(7, 7)),
                "רץ בפינת הלוח חייב להיות מסוגל לזוז עד לפינה הנגדית לאורך האלכסון הפנוי");
    }
}
