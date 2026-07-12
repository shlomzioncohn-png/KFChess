
import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

    /**
     * מחלקת בדיקה עבור MatrixBoard.
     * מטרת הטסטים היא להריץ את כל שורות הקוד של הלוח ולוודא שהוא פועל לפי המפרט.
     */
    public class BoardTest {

        /**
         * טסט 1: בודק שהלוח שומר נכון את הממדים (רוחב וגובה) ולא תקוע על גודל קבוע.
         */
        @Test
        public void testBoardDimensions() {
            Board board = new MatrixBoard(4, 5);

            assertEquals(4, board.getWidth(), "הרוחב שנשמר בלוח חייב להיות 4");
            assertEquals(5, board.getHeight(), "הגובה שנשמר בלוח חייב להיות 5");
        }

        /**
         * טסט 2: בודק שפונקציית isValidPosition מזהה נכון מה בתוך הלוח ומה מחוץ לו.
         */
        @Test
        public void testValidAndInvalidPositions() {
            Board board = new MatrixBoard(8, 8);

            assertTrue(board.isValidPosition(new Position(0, 0)), "הפינה השמאלית העליונה (0,0) חוקית");
            assertTrue(board.isValidPosition(new Position(7, 7)), "הפינה הימנית התחתונה (7,7) חוקית");

            assertFalse(board.isValidPosition(new Position(8, 5)), "שורה 8 לא חוקית בלוח שהשורה המקסימלית שלו היא 7");
            assertFalse(board.isValidPosition(new Position(3, -1)), "עמודה שלילית (-1) היא מחוץ לגבולות הלוח");
            assertFalse(board.isValidPosition(null), "מיקום שהוא null אינו חוקי ואסור שיקרוס");
        }

        /**
         * טסט 3: מוודא שתא שעדיין לא שמו בו כלום מחזיר null (ריק) ולא זורק שגיאה.
         */
        @Test
        public void testEmptyCellReturnsNull() {
            Board board = new MatrixBoard(8, 8);
            Position pos = new Position(3, 3);

            // assertNull מוודא שהתוצאה שחוזרת מהפונקציה היא אכן null
            assertNull(board.getPieceAt(pos), "תא חדש בלוח חייב להתחיל כשהוא ריק (null)");
        }

        /**
         * טסט 4:  מוודאת שהוספת כלי עובדת, ושניסיון לשים כלי נוסף
         * באותו תא נחסם מיידית על ידי מנגנון "דחיית תפוסה כפולה".
         */
        @Test
        public void testAddPieceAndDoubleOccupancyRejection() {
            Board board = new MatrixBoard(8, 8);
            Position pos = new Position(4, 4); // נבחר את משבצת האמצע (4,4)

            // נצרף שני כלים שונים שרוצים להגיע לאותה משבצת
            Piece whitePawn = new Piece("p1", PieceColor.WHITE, PieceType.PAWN, pos);
            Piece blackRook = new Piece("r1", PieceColor.BLACK, PieceType.ROOK, pos);

            board.addPiece(pos, whitePawn);
            assertEquals(whitePawn, board.getPieceAt(pos), "הלוח צריך להחזיר את הרגלי הלבן מהמשבצת");

            assertThrows(IllegalStateException.class, () -> {
                board.addPiece(pos, blackRook);
            }, "הלוח חייב לזרוק IllegalStateException כשיש ניסיון לתפוסה כפולה");
        }

        /**
         * טסט 5: מוודא שהסרת כלי מהלוח מנקה את המטריצה ומחזירה את התא למצב ריק.
         */
        @Test
        public void testRemovePieceClearsCell() {
            Board board = new MatrixBoard(8, 8);
            Position pos = new Position(2, 2);
            Piece piece = new Piece("p2", PieceColor.WHITE, PieceType.PAWN, pos);

            board.addPiece(pos, piece);

            board.removePiece(pos);

            assertNull(board.getPieceAt(pos), "אחרי הסרת הכלי, המשבצת חייבת לחזור להיות null");
        }
    }

