import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import static org.junit.jupiter.api.Assertions.*;

public class RuleEngineTest {

    @Test
    void nullBoardOrPositionsReturnFalse() {
        Board board = new MatrixBoard(8, 8);
        Position pos = new Position(0, 0);

        assertFalse(RuleEngine.validateMove(null, pos, pos), "לוח null חייב לגרום למהלך לא חוקי");
        assertFalse(RuleEngine.validateMove(board, null, pos), "מיקום מקור null חייב לגרום למהלך לא חוקי");
        assertFalse(RuleEngine.validateMove(board, pos, null), "מיקום יעד null חייב לגרום למהלך לא חוקי");
    }

    @Test
    void invalidPositionsReturnFalse() {
        Board board = new MatrixBoard(4, 4);
        board.addPiece(new Position(0, 0), new Piece("r", PieceColor.WHITE, PieceType.ROOK, new Position(0, 0)));

        assertFalse(RuleEngine.validateMove(board, new Position(0, 0), new Position(9, 9)),
                "מהלך אל מיקום מחוץ ללוח חייב להיות לא חוקי");
        assertFalse(RuleEngine.validateMove(board, new Position(-1, 0), new Position(0, 0)),
                "מהלך ממיקום מחוץ ללוח חייב להיות לא חוקי");
    }

    @Test
    void movingToSamePositionReturnsFalse() {
        Board board = new MatrixBoard(4, 4);
        Position pos = new Position(1, 1);
        board.addPiece(pos, new Piece("r", PieceColor.WHITE, PieceType.ROOK, pos));

        assertFalse(RuleEngine.validateMove(board, pos, pos), "מהלך שהיעד שלו זהה למקור חייב להיות לא חוקי");
    }

    @Test
    void movingFromEmptyCellReturnsFalse() {
        Board board = new MatrixBoard(4, 4);

        assertFalse(RuleEngine.validateMove(board, new Position(0, 0), new Position(1, 1)),
                "אין כלי במשבצת המקור, אז המהלך חייב להיות לא חוקי");
    }

    @Test
    void capturingOwnColorReturnsFalse() {
        Board board = new MatrixBoard(4, 4);
        Position src = new Position(0, 0);
        Position dest = new Position(0, 1);
        board.addPiece(src, new Piece("r", PieceColor.WHITE, PieceType.ROOK, src));
        board.addPiece(dest, new Piece("friend", PieceColor.WHITE, PieceType.PAWN, dest));

        assertFalse(RuleEngine.validateMove(board, src, dest), "לא ניתן לתפוס כלי מאותו הצבע");
    }

    @Test
    void delegatesToPieceSpecificRuleForLegalMove() {
        Board board = new MatrixBoard(4, 4);
        Position src = new Position(0, 0);
        board.addPiece(src, new Piece("r", PieceColor.WHITE, PieceType.ROOK, src));

        assertTrue(RuleEngine.validateMove(board, src, new Position(0, 3)),
                "מהלך חוקי לפי כללי הכלי הספציפי חייב לעבור");
    }
}
