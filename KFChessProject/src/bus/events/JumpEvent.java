package bus.events;

import models.Position;

public class JumpEvent {
    private final Position position;

    public JumpEvent(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}