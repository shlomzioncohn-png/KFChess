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

        Piece piece = board.getPieceAt(src);

        if (piece == null) {
            return;
        }

        if (piece.getState() == PieceState.AIRBORNE) {
            return;
        }

        MoveResult result = arbiter.validateMove(board, src, dest);

        if (result.isSuccess()) {
            piece.setState(PieceState.AIRBORNE);

            Motion motion = new Motion(
                    src,
                    dest,
                    currentClock + result.getTravelTime()
            );

            activeMotions.add(motion);
        }
    }



    public void update(long currentClock) {
        activeMotions.removeIf(motion -> {

            if (currentClock < motion.getArrivalTime()) {
                return false;
            }

            Position src = motion.getSource();
            Position dest = motion.getDestination();

            Piece movingPiece = board.getPieceAt(src);

            if (movingPiece == null) {
                return true;
            }

            Piece target = board.getPieceAt(dest);

            if (target != null &&
                    target.getColor() != movingPiece.getColor()) {

                board.removePiece(dest);
            }

            board.movePiece(src, dest);

            movingPiece.setState(PieceState.IDLE);

            return true;
        });

    }
}
