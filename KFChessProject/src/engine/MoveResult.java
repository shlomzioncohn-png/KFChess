package engine;

import models.Position;

public class MoveResult {

    private final boolean success;
    private final Position src;
    private final Position dest;
    private final String message;
    private final long travelTime;

    public MoveResult(boolean success, Position src, Position dest, String message, long travelTime) {
        this.success = success;
        this.src = src;
        this.dest = dest;
        this.message = message;
        this.travelTime = travelTime;
    }

    public boolean isSuccess() { return success; }
    public long getTravelTime() { return travelTime; }
}
