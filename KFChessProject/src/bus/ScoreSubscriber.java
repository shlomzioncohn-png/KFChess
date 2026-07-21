package bus;

import bus.events.CaptureEvent;
import models.GameState;
import models.Piece;
import rules.PieceValue;

public class ScoreSubscriber implements EventListener {

    private final GameState gameState;

    public ScoreSubscriber(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (!(payload instanceof CaptureEvent event)) return;

        Piece attacker = event.getAttacker();
        Piece captured = event.getCaptured();

        int points = PieceValue.getValue(captured.getType());
        gameState.addScore(attacker.getColor(), points);
    }
}