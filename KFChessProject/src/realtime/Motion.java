package realtime;

import models.Piece;
import models.Position;

public class Motion {

    private final Piece piece;
    private final Position source;
    private final Position destination;
    private final long arrivalTime;

    public Motion(Piece piece, Position source, Position destination, long arrivalTime) {
        this.piece = piece;
        this.source = source;
        this.destination = destination;
        this.arrivalTime = arrivalTime;
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

    public long getArrivalTime() {
        return arrivalTime;
    }
}