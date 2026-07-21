package server;

public class EloCalculator {

    private static final int K_FACTOR = 32;

    public static int[] calculateNewRatings(int winnerRating, int loserRating) {
        double expectedWinner = 1.0 / (1.0 + Math.pow(10, (loserRating - winnerRating) / 400.0));
        double expectedLoser = 1.0 / (1.0 + Math.pow(10, (winnerRating - loserRating) / 400.0));

        int newWinnerRating = (int) Math.round(winnerRating + K_FACTOR * (1 - expectedWinner));
        int newLoserRating = (int) Math.round(loserRating + K_FACTOR * (0 - expectedLoser));

        return new int[] { newWinnerRating, newLoserRating };
    }
}