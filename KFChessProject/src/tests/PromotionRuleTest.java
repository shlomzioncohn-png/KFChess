package  tests;

import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.PromotionRule;

import static org.junit.jupiter.api.Assertions.*;

public class PromotionRuleTest {

    @Test
    void whitePawnIsEligibleAtRowZero() {
        Board board = new MatrixBoard(8, 8);
        Piece pawn = new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(1, 0));

        assertTrue(PromotionRule.isEligible(board, pawn, new Position(0, 0)),
                "רגלי לבן שמגיע לשורה 0 חייב להיות זכאי להכתרה");
    }

    @Test
    void blackPawnIsEligibleAtLastRow() {
        Board board = new MatrixBoard(8, 8);
        Piece pawn = new Piece("bp", PieceColor.BLACK, PieceType.PAWN, new Position(6, 0));

        assertTrue(PromotionRule.isEligible(board, pawn, new Position(7, 0)),
                "רגלי שחור שמגיע לשורה האחרונה חייב להיות זכאי להכתרה");
    }

    @Test
    void pawnNotOnLastRowIsNotEligible() {
        Board board = new MatrixBoard(8, 8);
        Piece pawn = new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(3, 0));

        assertFalse(PromotionRule.isEligible(board, pawn, new Position(2, 0)),
                "רגלי שלא הגיע לשורה האחרונה אינו זכאי להכתרה");
    }

    @Test
    void nonPawnPieceIsNeverEligible() {
        Board board = new MatrixBoard(8, 8);
        Piece rook = new Piece("wr", PieceColor.WHITE, PieceType.ROOK, new Position(1, 0));

        assertFalse(PromotionRule.isEligible(board, rook, new Position(0, 0)),
                "כלי שאינו רגלי לעולם אינו זכאי להכתרה, גם אם הגיע לשורה האחרונה");
    }

    @Test
    void promotedTypeIsAlwaysQueen() {
        assertEquals(PieceType.QUEEN, PromotionRule.promotedType(), "כלי מוכתר תמיד הופך למלכה");
    }
}
