package bus.events;

import models.Piece;
import models.Position;

public class CaptureEvent {

    private final Piece attacker;
    private final Piece captured;
    private final Position source;
    private final Position destination;

    public CaptureEvent(Piece attacker, Piece captured, Position source, Position destination) {
        this.attacker = attacker;
        this.captured = captured;
        this.source = source;
        this.destination = destination;
    }

    public Piece getAttacker() {
        return attacker;
    }

    public Piece getCaptured() {
        return captured;
    }

    public Position getSource() {
        return source;
    }

    public Position getDestination() {
        return destination;
    }
}