package engine;

import models.Board;
import models.Piece;
import models.Position;
import models.enums.PieceState;
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
                Position dest = motion.getDestination();
                Position src = motion.getSource();

                Piece target = board.getPieceAt(dest);
                if (target != null && target.getState() == PieceState.AIRBORNE) {
                    board.removePiece(src);
                }
                else {
                    Piece pieceToMove = board.getPieceAt(src);
                    if (pieceToMove != null && pieceToMove.getState() != PieceState.AIRBORNE) {
                        board.movePiece(src, dest);
                        pieceToMove.setState(PieceState.IDLE);
                    }
                }
                return true;
            }
            return false;
        });}

}
