package bus.events;

import models.Piece;
import models.Position;

public class MoveEvent {

    private final Piece piece;
    private final Position source;
    private final Position destination;
    private final boolean wasCapture;
    private final long gameClockMs;

    public MoveEvent(Piece piece, Position source, Position destination, boolean wasCapture, long gameClockMs) {
        this.piece = piece;
        this.source = source;
        this.destination = destination;
        this.wasCapture = wasCapture;
        this.gameClockMs = gameClockMs;
    }

    public long getGameClockMs() {
        return gameClockMs;
    }

    public Piece getPiece() {
        return piece;
    }

    public Position getSource() {
        return source;
    }

    public Position getDestination() {
        return destination;
    }

    public boolean wasCapture() {
        return wasCapture;
    }
}