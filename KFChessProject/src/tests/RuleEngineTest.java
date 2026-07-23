package  tests;

import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import rules.RuleEngine;

import java.util.List;

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

    @Test
    void getLegalDestinationsForRookInOpenBoardReturnsAllStraightLineCells() {
        Board board = new MatrixBoard(8, 8);
        Position rookPos = new Position(4, 4);
        board.addPiece(rookPos, new Piece("r", PieceColor.WHITE, PieceType.ROOK, rookPos));

        List<Position> legal = new RuleEngine().getLegalDestinations(board, rookPos);

        assertEquals(14, legal.size(), "צריח בודד באמצע לוח פתוח 8x8 חייב לקבל בדיוק 14 יעדים (7 בשורה + 7 בעמודה)");
        assertTrue(legal.contains(new Position(4, 0)));
        assertTrue(legal.contains(new Position(0, 4)));
        assertFalse(legal.contains(rookPos), "המשבצת הנוכחית של הכלי אסורה להופיע כיעד חוקי");
        assertFalse(legal.contains(new Position(5, 5)), "יעד אלכסוני אסור להופיע ביעדים החוקיים של צריח");
    }

    @Test
    void getLegalDestinationsFromEmptyCellReturnsEmptyList() {
        Board board = new MatrixBoard(8, 8);

        List<Position> legal = new RuleEngine().getLegalDestinations(board, new Position(0, 0));

        assertTrue(legal.isEmpty(), "אין כלי במשבצת המקור - רשימת היעדים החוקיים חייבת להיות ריקה, לא לזרוק שגיאה");
    }

    @Test
    void getLegalDestinationsExcludesSquaresOccupiedByOwnColor() {
        Board board = new MatrixBoard(8, 8);
        Position rookPos = new Position(4, 4);
        board.addPiece(rookPos, new Piece("r", PieceColor.WHITE, PieceType.ROOK, rookPos));
        Position blockerPos = new Position(4, 6);
        board.addPiece(blockerPos, new Piece("friend", PieceColor.WHITE, PieceType.PAWN, blockerPos));

        List<Position> legal = new RuleEngine().getLegalDestinations(board, rookPos);

        assertFalse(legal.contains(blockerPos), "משבצת עם כלי-בית אסורה להופיע ביעדים החוקיים");
        assertFalse(legal.contains(new Position(4, 7)), "משבצת שמעבר לכלי-הבית חסומה ואסורה להופיע ביעדים החוקיים");
        assertTrue(legal.contains(new Position(4, 5)), "המשבצת שלפני כלי-הבית עדיין חייבת להיות יעד חוקי");
    }
}
