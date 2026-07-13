package realtime;

import models.Position;

public class Motion {

    private final Position source;
    private final Position destination;
    private final long arrivalTime;

    public Motion(Position source, Position destination, long arrivalTime) {
        this.source = source;
        this.destination = destination;
        this.arrivalTime = arrivalTime;
    }

    public Position getSource() {
        return source;
    }

    public Position getDestination() {
        return destination;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }
}
