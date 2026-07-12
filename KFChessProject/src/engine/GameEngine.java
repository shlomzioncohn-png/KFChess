package engine;

import models.Board;
import models.Position;
import realtime.Motion;
import realtime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private final Board board;
    private final RealTimeArbiter arbiter;
    private final List<Motion> activeMotions;

    public GameEngine(Board board, RealTimeArbiter arbiter) {
        this.board = board;
        this.arbiter = arbiter;
        this.activeMotions = new ArrayList<>();
    }

    public void tryMove(Position src, Position dest, long currentClock) {
        MoveResult result = arbiter.validateMove(board, src, dest);

        if (result.isSuccess()) {
            Motion motion = new Motion(src, dest, currentClock + result.getTravelTime());
            activeMotions.add(motion);
        }
    }

    public void update(long currentClock) {
        activeMotions.removeIf(motion -> {
            if (currentClock >= motion.getArrivalTime()) {
                board.movePiece(motion.getSource(), motion.getDestination());
                return true;
            }
            return false;
        });
    }

}
