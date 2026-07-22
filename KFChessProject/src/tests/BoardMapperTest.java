package tests;

import input.BoardMapper;
import models.Board;
import models.MatrixBoard;
import models.Position;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;



public class BoardMapperTest {

    @Test
    void mapsPixelCoordinatesToCorrectCell() {
        Board board = new MatrixBoard(8, 8);
        Position pos = BoardMapper.mapPixelToPosition(250, 150, board, 100);

        assertNotNull(pos, "קואורדינטת פיקסל תקינה חייבת להניב מיקום לוגי");
        assertEquals(1, pos.getRow(), "השורה חייבת להיות y / CELL_SIZE");
        assertEquals(2, pos.getCol(), "העמודה חייבת להיות x / CELL_SIZE");
    }

    @Test
    void returnsNullWhenResultingPositionIsOutOfBounds() {
        Board board = new MatrixBoard(2, 2); // לוח קטן, 2x2 בלבד
        Position pos = BoardMapper.mapPixelToPosition(500, 500, board, 100);

        assertNull(pos, "פיקסל שממופה מחוץ לגבולות הלוח חייב להחזיר null");
    }

    @Test
    void returnsNullWhenBoardIsNull() {
        Position pos = BoardMapper.mapPixelToPosition(50, 50, null, 100);
        assertNull(pos, "כאשר הלוח הוא null, חייב להיות מוחזר null ולא לקרוס");
    }
}
