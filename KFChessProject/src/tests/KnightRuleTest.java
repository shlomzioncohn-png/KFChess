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

public class KnightRuleTest {

    private Board boardWithKnight(Position pos) {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(pos, new Piece("n1", PieceColor.WHITE, PieceType.KNIGHT, pos));
        return board;
    }

    @Test
    void knightMovesInLShape() {
        Position knightPos = new Position(4, 4);
        Board board = boardWithKnight(knightPos);

        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(6, 5)), "פרש חייב לזוז בצורת L (2+1)");
        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(2, 3)), "פרש חייב לזוז בצורת L (2+1) לכיוון ההפוך");
        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(5, 6)), "פרש חייב לזוז בצורת L (1+2)");
    }

    @Test
    void knightCannotMoveStraightOrDiagonally() {
        Position knightPos = new Position(4, 4);
        Board board = boardWithKnight(knightPos);

        assertFalse(RuleEngine.validateMove(board, knightPos, new Position(4, 6)), "פרש אינו יכול לזוז בקו ישר");
        assertFalse(RuleEngine.validateMove(board, knightPos, new Position(6, 6)), "פרש אינו יכול לזוז באלכסון");
    }

    @Test
    void knightCanJumpOverOtherPieces() {
        Position knightPos = new Position(4, 4);
        Board board = boardWithKnight(knightPos);
        // חוסמים את כל המשבצות שבין המקור ליעד - לא אמור לשנות כלום עבור פרש
        board.addPiece(new Position(4, 5), new Piece("blk1", PieceColor.WHITE, PieceType.PAWN, new Position(4, 5)));
        board.addPiece(new Position(5, 4), new Piece("blk2", PieceColor.WHITE, PieceType.PAWN, new Position(5, 4)));
        board.addPiece(new Position(5, 5), new Piece("blk3", PieceColor.WHITE, PieceType.PAWN, new Position(5, 5)));

        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(6, 5)),
                "פרש מדלג מעל כלים אחרים, לכן חסימות בדרך אינן אמורות להשפיע");
    }

    @Test
    void knightCannotCaptureOwnColor() {
        Position knightPos = new Position(4, 4);
        Board board = boardWithKnight(knightPos);
        Position friendPos = new Position(6, 5);
        board.addPiece(friendPos, new Piece("friend", PieceColor.WHITE, PieceType.PAWN, friendPos));

        assertFalse(RuleEngine.validateMove(board, knightPos, friendPos), "פרש אינו יכול לתפוס כלי מאותו הצבע");
    }

    @Test
    void knightAtCornerHasOnlyTheTwoLShapedMovesThatStayOnBoard() {
        Position knightPos = new Position(0, 0);
        Board board = boardWithKnight(knightPos);

        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(1, 2)), "פרש בפינה (0,0) - מהלך L חוקי אחד");
        assertTrue(RuleEngine.validateMove(board, knightPos, new Position(2, 1)), "פרש בפינה (0,0) - מהלך L חוקי שני");
        assertFalse(RuleEngine.validateMove(board, knightPos, new Position(-1, 2)),
                "מהלך שהיה חוקי בצורתו אך יוצא מגבולות הלוח חייב להיפסל");
    }
}
