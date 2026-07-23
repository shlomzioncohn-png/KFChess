package  tests;

import models.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {

    @Test
    void gettersReturnConstructorValues() {
        Position p = new Position(3, 5);
        assertEquals(3, p.getRow(), "השורה חייבת להיות זהה לזו שהוזנה בבנאי");
        assertEquals(5, p.getCol(), "העמודה חייבת להיות זהה לזו שהוזנה בבנאי");
    }

    @Test
    void equalPositionsAreEqual() {
        Position a = new Position(2, 4);
        Position b = new Position(2, 4);
        assertEquals(a, b, "שני מיקומים עם אותה שורה ועמודה חייבים להיות שווים");
        assertEquals(a.hashCode(), b.hashCode(), "מיקומים שווים חייבים להחזיר hashCode זהה");
    }

    @Test
    void differentPositionsAreNotEqual() {
        Position a = new Position(2, 4);
        Position b = new Position(4, 2);
        assertNotEquals(a, b, "מיקומים עם ערכים שונים אינם אמורים להיות שווים");
    }

    @Test
    void positionIsEqualToItself() {
        Position a = new Position(1, 1);
        assertEquals(a, a, "מיקום חייב להיות שווה לעצמו");
    }

    @Test
    void positionIsNotEqualToNullOrOtherType() {
        Position a = new Position(1, 1);
        assertNotEquals(null, a, "מיקום אינו אמור להיות שווה ל-null");
        assertNotEquals("not a position", a, "מיקום אינו אמור להיות שווה לאובייקט מטיפוס אחר");
    }

    @Test
    void toStringHasExpectedFormat() {
        Position p = new Position(6, 7);
        assertEquals("(6,7)", p.toString(), "הפורמט של toString חייב להיות (row,col)");
    }
}
