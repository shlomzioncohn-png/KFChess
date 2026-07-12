import io.BoardParser;
import models.Board;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardParserTest {

    @Test
    public void testParseValidBoardString() {
        String rawBoard = "wP . . bK\n" +
                ". . . .\n" +
                ". wR . .";

        Board board = BoardParser.parse(rawBoard);

        assertNotNull(board, "הלוח שנוצר אסור שיהיה null");
        assertEquals(4, board.getWidth(), "רוחב הלוח צריך להיות 4 עמודות");
        assertEquals(3, board.getHeight(), "גובה הלוח צריך להיות 3 שורות");

        assertNotNull(board.getPieceAt(new Position(0, 0)));
        assertEquals(PieceColor.WHITE, board.getPieceAt(new Position(0, 0)).getColor());
        assertEquals(PieceType.PAWN, board.getPieceAt(new Position(0, 0)).getType());

        assertNotNull(board.getPieceAt(new Position(2, 1)));
        assertEquals(PieceType.ROOK, board.getPieceAt(new Position(2, 1)).getType());

        assertNull(board.getPieceAt(new Position(0, 1)), "משבצת עם נקודה צריכה להישאר ריקה");
    }

    @Test
    public void testParseEmptyOrNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse("");
        }, "טקסט ריק חייב לזרוק שגיאה");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(null);
        }, "טקסט null חייב לזרוק שגיאה");
    }
}
