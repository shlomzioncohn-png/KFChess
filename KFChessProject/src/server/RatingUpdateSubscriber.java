package server;

import bus.EventListener;
import bus.events.GameOverEvent;
import models.enums.PieceColor;

public class RatingUpdateSubscriber implements EventListener {

    private final GameSession session;

    public RatingUpdateSubscriber(GameSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (!(payload instanceof GameOverEvent event)) return;

        PlayerRole winnerRole = event.getWinner() == PieceColor.WHITE ? PlayerRole.WHITE : PlayerRole.BLACK;
        PlayerRole loserRole = winnerRole == PlayerRole.WHITE ? PlayerRole.BLACK : PlayerRole.WHITE;

        String winnerName = session.getUsernameByRole(winnerRole);
        String loserName = session.getUsernameByRole(loserRole);

        if (winnerName == null || loserName == null) {
            System.out.println("[RATING] could not resolve player names, skipping update");
            return;
        }

        int winnerRating = DatabaseManager.getRating(winnerName);
        int loserRating = DatabaseManager.getRating(loserName);

        int[] newRatings = EloCalculator.calculateNewRatings(winnerRating, loserRating);

        DatabaseManager.updateRating(winnerName, newRatings[0]);
        DatabaseManager.updateRating(loserName, newRatings[1]);

        System.out.println("[RATING] " + winnerName + ": " + winnerRating + " -> " + newRatings[0]
                + " | " + loserName + ": " + loserRating + " -> " + newRatings[1]);
    }
}
