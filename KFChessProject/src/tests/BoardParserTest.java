package  tests;

import io.BoardParser;
import models.Board;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardParserTest {

    @Test
    void parsesSimpleBoardWithPiecesAndEmptyCells() {
        String text = "wP . bR .\n. . . .\n. . . .";

        Board board = BoardParser.parse(text);

        assertNotNull(board, "הלוח שנוצר אסור שיהיה null");
        assertEquals(4, board.getWidth(), "רוחב הלוח צריך להיות 4 עמודות");
        assertEquals(3, board.getHeight(), "גובה הלוח צריך להיות 3 שורות");

        assertNotNull(board.getPieceAt(new Position(0, 0)));
        assertEquals(PieceColor.WHITE, board.getPieceAt(new Position(0, 0)).getColor());
        assertEquals(PieceType.PAWN, board.getPieceAt(new Position(0, 0)).getType());

        assertNotNull(board.getPieceAt(new Position(0, 2)));
        assertEquals(PieceType.ROOK, board.getPieceAt(new Position(0, 2)).getType());
        assertEquals(PieceColor.BLACK, board.getPieceAt(new Position(0, 2)).getColor());

        assertNull(board.getPieceAt(new Position(0, 1)), "משבצת עם נקודה צריכה להישאר ריקה");
    }

    @Test
    void parsesAllPieceTypes() {
        String text = "wK wQ wR wB wN wP";

        Board board = BoardParser.parse(text);

        assertEquals(PieceType.KING, board.getPieceAt(new Position(0, 0)).getType());
        assertEquals(PieceType.QUEEN, board.getPieceAt(new Position(0, 1)).getType());
        assertEquals(PieceType.ROOK, board.getPieceAt(new Position(0, 2)).getType());
        assertEquals(PieceType.BISHOP, board.getPieceAt(new Position(0, 3)).getType());
        assertEquals(PieceType.KNIGHT, board.getPieceAt(new Position(0, 4)).getType());
        assertEquals(PieceType.PAWN, board.getPieceAt(new Position(0, 5)).getType());
    }

    @Test
    void pieceIdsAreUnique() {
        String text = "wP wP\nbP bP";
        Board board = BoardParser.parse(text);

        String id1 = board.getPieceAt(new Position(0, 0)).getId();
        String id2 = board.getPieceAt(new Position(0, 1)).getId();
        String id3 = board.getPieceAt(new Position(1, 0)).getId();

        assertNotEquals(id1, id2, "לכל כלי חייב מזהה ייחודי משלו");
        assertNotEquals(id2, id3, "לכל כלי חייב מזהה ייחודי משלו");
    }

    @Test
    void nullInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> BoardParser.parse(null),
                "קלט null חייב לזרוק IllegalArgumentException");
    }

    @Test
    void emptyInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> BoardParser.parse("   "),
                "קלט ריק חייב לזרוק IllegalArgumentException");
    }

    @Test
    void mismatchedRowWidthThrows() {
        String text = "wP wP wP\nbP bP";
        assertThrows(IllegalArgumentException.class, () -> BoardParser.parse(text),
                "שורות עם מספר עמודות שונה חייבות לזרוק IllegalArgumentException");
    }

    @Test
    void unknownTokenThrows() {
        String text = "wP xZ";
        assertThrows(IllegalArgumentException.class, () -> BoardParser.parse(text),
                "טוקן לא מוכר (למשל xZ) חייב לזרוק IllegalArgumentException");
    }

    @Test
    void invalidColorCharThrows() {
        String text = "zP .";
        assertThrows(IllegalArgumentException.class, () -> BoardParser.parse(text),
                "אות צבע לא חוקית חייבת לזרוק IllegalArgumentException");
    }
}
