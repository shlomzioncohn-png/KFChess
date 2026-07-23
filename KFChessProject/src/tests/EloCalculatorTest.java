package  tests;

import org.junit.jupiter.api.Test;
import server.EloCalculator;

import static org.junit.jupiter.api.Assertions.*;

public class EloCalculatorTest {

    @Test
    void equalRatingsSplitTheFullKFactorEvenly() {
        int[] result = EloCalculator.calculateNewRatings(1200, 1200);

        assertEquals(1216, result[0], "בדירוגים שווים המנצח חייב לעלות בדיוק ב-K/2 = 16");
        assertEquals(1184, result[1], "בדירוגים שווים המפסיד חייב לרדת בדיוק ב-K/2 = 16");
    }

    @Test
    void favoriteWinningGainsFewerPointsThanExpected() {
        // דירוג 1400 מנצח דירוג 1200 - התוצאה הצפויה, לכן רווח קטן מ-K/2
        int[] result = EloCalculator.calculateNewRatings(1400, 1200);

        assertEquals(1408, result[0], "מועדף שמנצח כצפוי חייב להרוויח מעט בלבד");
        assertEquals(1192, result[1], "המפסיד חייב להפסיד באותה כמות בדיוק (משחק סכום-אפס)");
    }

    @Test
    void underdogWinningGainsMorePointsThanFavorite() {
        // דירוג 1200 מנצח דירוג 1400 - הפתעה, לכן רווח גדול מ-K/2
        int[] result = EloCalculator.calculateNewRatings(1200, 1400);

        assertEquals(1224, result[0], "מנצח שהיה חלש יותר בדירוג (הפתעה) חייב להרוויח יותר מ-16 נקודות");
        assertEquals(1376, result[1], "המפסיד המדורג גבוה חייב להפסיד יותר מ-16 נקודות");
        assertTrue(result[0] - 1200 > 1408 - 1400,
                "רווח בניצחון-הפתעה חייב להיות גדול יותר מרווח בניצחון צפוי");
    }

    @Test
    void extremeGapFavoriteWinIsRoundedAwayToZeroChange() {
        // פער ענק - שני הצדדים כה קרובים לתוצאה הצפויה שהשינוי מתעגל ל-0 בשני הכיוונים
        int[] result = EloCalculator.calculateNewRatings(2400, 1200);

        assertEquals(2400, result[0], "בפער קיצוני, ניצחון צפוי לחלוטין מתעגל לשינוי אפס במנצח");
        assertEquals(1200, result[1], "בפער קיצוני, הפסד צפוי לחלוטין מתעגל לשינוי אפס במפסיד");
    }

    @Test
    void extremeGapUpsetGrantsNearlyTheFullKFactor() {
        int[] result = EloCalculator.calculateNewRatings(1200, 2400);

        assertEquals(1232, result[0], "הפתעה קיצונית חייבת להעניק למנצח כמעט את מלוא ה-K (32)");
        assertEquals(2368, result[1], "הפתעה קיצונית חייבת לגרוע מהמפסיד כמעט את מלוא ה-K (32)");
    }

    @Test
    void winnerRatingNeverDecreasesAndLoserRatingNeverIncreases() {
        int[] closeGame = EloCalculator.calculateNewRatings(1500, 1490);
        int[] hugeUpset = EloCalculator.calculateNewRatings(1000, 2200);
        int[] extremeFavorite = EloCalculator.calculateNewRatings(2200, 1000);

        assertTrue(closeGame[0] >= 1500, "המנצח אסור לו לרדת בדירוג לעולם");
        assertTrue(closeGame[1] <= 1490, "המפסיד אסור לו לעלות בדירוג לעולם");
        assertTrue(hugeUpset[0] >= 1000);
        assertTrue(hugeUpset[1] <= 2200);
        assertTrue(extremeFavorite[0] >= 2200);
        assertTrue(extremeFavorite[1] <= 1000);
    }

    @Test
    void ratingChangeIsAlwaysZeroSum() {
        int[] r1 = EloCalculator.calculateNewRatings(1250, 1000);
        int winnerGain1 = r1[0] - 1250;
        int loserChange1 = r1[1] - 1000;
        assertEquals(0, winnerGain1 + loserChange1,
                "סך השינוי חייב להיות אפס - כמה שהמנצח מרוויח, המפסיד מפסיד בדיוק אותו הדבר");

        int[] r2 = EloCalculator.calculateNewRatings(900, 2000);
        int winnerGain2 = r2[0] - 900;
        int loserChange2 = r2[1] - 2000;
        assertEquals(0, winnerGain2 + loserChange2, "אותה בדיקה עבור פער גדול והפוך");
    }

    @Test
    void lowRatingsStillProduceSymmetricResultAroundZero() {
        int[] result = EloCalculator.calculateNewRatings(0, 0);

        assertEquals(16, result[0], "גם בדירוג 0, הנוסחה חייבת להתנהג זהה לכל דירוג שווה אחר");
        assertEquals(-16, result[1], "מפסיד עם דירוג 0 יכול לרדת מתחת לאפס - אין הגנה כזו בנוסחה");
    }
}
