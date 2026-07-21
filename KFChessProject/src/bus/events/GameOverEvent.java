package bus.events;

import models.enums.PieceColor;

public class GameOverEvent {

    private final PieceColor winner;

    public GameOverEvent(PieceColor winner) {
        this.winner = winner;
    }

    public PieceColor getWinner() {
        return winner;
    }
}