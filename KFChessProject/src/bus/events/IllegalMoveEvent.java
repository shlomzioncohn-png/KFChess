package bus.events;

import models.Position;

public class IllegalMoveEvent {

    private final Position from;
    private final Position to;

    public IllegalMoveEvent(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }
}
