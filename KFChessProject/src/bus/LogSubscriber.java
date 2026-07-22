package bus.subscribers;

import bus.EventListener;
import bus.events.MoveEvent;
import models.GameState;
import models.Piece;

public class LogSubscriber implements EventListener {

    private final GameState gameState;

    public LogSubscriber(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (!(payload instanceof MoveEvent event)) return;

        Piece piece = event.getPiece();
        String description = piece.getType()
                + " " + event.getSource() + " -> " + event.getDestination()
                + (event.wasCapture() ? " (capture)" : "");

        gameState.addLogEntry(piece.getColor(), description, event.getGameClockMs());
    }
}