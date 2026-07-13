import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class PawnRuleTest {

    @Test
    void whitePawnMovesOneStepForwardWhenEmpty() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));

        assertTrue(RuleEngine.validateMove(board, pawnPos, new Position(2, 3)),
                "רגלי לבן חייב להיות מסוגל לזוז צעד אחד קדימה (כלפי מעלה) למשבצת ריקה");
    }

    @Test
    void whitePawnCannotMoveForwardIntoOccupiedCell() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));
        Position blocked = new Position(2, 3);
        board.addPiece(blocked, new Piece("blocker", PieceColor.BLACK, PieceType.PAWN, blocked));

        assertFalse(RuleEngine.validateMove(board, pawnPos, blocked),
                "רגלי אינו יכול להתקדם ישר קדימה אל תוך משבצת תפוסה (גם לא לתפוס ישר קדימה)");
    }

    @Test
    void whitePawnCanMoveTwoStepsFromStartRowWhenPathClear() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(7, 3); // שורת ההתחלה של הלבן היא height - 1
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));

        assertTrue(RuleEngine.validateMove(board, pawnPos, new Position(5, 3)),
                "רגלי לבן בשורת ההתחלה חייב להיות מסוגל לזוז 2 צעדים קדימה כשהדרך פנויה");
    }

    @Test
    void whitePawnTwoStepMoveBlockedByPieceInPath() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(7, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));
        board.addPiece(new Position(6, 3), new Piece("blocker", PieceColor.WHITE, PieceType.PAWN, new Position(6, 3)));

        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(5, 3)),
                "מהלך של 2 צעדים חייב להיכשל אם יש כלי חוסם בדרך");
    }

    @Test
    void whitePawnCannotMoveTwoStepsWhenNotOnStartRow() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));

        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(1, 3)),
                "רגלי שאינו בשורת ההתחלה אינו יכול לזוז 2 צעדים");
    }

    @Test
    void whitePawnCanCaptureDiagonally() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));
        Position enemyPos = new Position(2, 4);
        board.addPiece(enemyPos, new Piece("enemy", PieceColor.BLACK, PieceType.PAWN, enemyPos));

        assertTrue(RuleEngine.validateMove(board, pawnPos, enemyPos), "רגלי חייב להיות מסוגל לתפוס כלי יריב באלכסון");
    }

    @Test
    void whitePawnCannotMoveDiagonallyWithoutCapture() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));

        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(2, 4)),
                "רגלי אינו יכול לזוז באלכסון אם אין שם כלי לתפוס");
    }

    @Test
    void whitePawnCannotMoveSideways() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));

        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(3, 4)), "רגלי אינו יכול לזוז הצידה");
    }

    @Test
    void whitePawnCannotMoveBackward() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(3, 3);
        board.addPiece(pawnPos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pawnPos));

        assertFalse(RuleEngine.validateMove(board, pawnPos, new Position(4, 3)), "רגלי לבן אינו יכול לזוז אחורה");
    }

    @Test
    void blackPawnMovesDownward() {
        Board board = new MatrixBoard(8, 8);
        Position pawnPos = new Position(0, 3); // שורת ההתחלה של השחור היא 0
        board.addPiece(pawnPos, new Piece("bp", PieceColor.BLACK, PieceType.PAWN, pawnPos));

        assertTrue(RuleEngine.validateMove(board, pawnPos, new Position(1, 3)), "רגלי שחור חייב לזוז כלפי מטה");
        assertTrue(RuleEngine.validateMove(board, pawnPos, new Position(2, 3)),
                "רגלי שחור בשורת ההתחלה חייב להיות מסוגל לזוז 2 צעדים כשהדרך פנויה");
    }
}
