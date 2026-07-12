import io.BoardParser;
import models.Board;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardParserTest {

    /**
     * מוודא שה-Parser יודע לקרוא טקסט, לחלץ ממנו את גובה ורוחב הלוח הנכונים,
     * ולמקם את הכלים בדיוק במשבצות הנכונה.
     */
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

    /**
     * בודק מקרי קצה של קלט פגום (טקסט ריק או null) ומוודא שהמערכת חוסמת אותם וזורקת שגיאה
     */
    @Test
    public void testParseEmptyOrNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse("");
        }, "טקסט ריק חייב לזרוק שגיאה");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(null);
        }, "טקסט null חייב לזרוק שגיאה");
    }


    /**
     *         // בודק מה קורה אם שורה אחת ארוכה ושורה שנייה קצרה
     */
    @Test
    public void testParseAsymmetricBoardThrowsException() {
        String asymmetricBoard = "wP . . bK\n" +
                ". .";

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(asymmetricBoard);
        }, "ה-Parser חייב לזהות לוח לא סימטרי ולזרוק שגיאה");
    }

    /**
     *
     *      בודק מה קורה אם יש מילה מוזרה שלא תואמת לשום כלי
     */
    @Test
    public void testParseInvalidPieceTokenThrowsException() {
        String invalidPieceBoard = "wP . . bK\n" +
                ". hello . .";

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(invalidPieceBoard);
        }, "ה-Parser חייב לזהות טקסט של כלי לא חוקי ולזרוק שגיאה");
    }
}
