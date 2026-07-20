package engine;

import bus.EventBus;
import bus.events.CaptureEvent;
import bus.events.GameOverEvent;
import bus.events.JumpEvent;
import bus.events.MoveEvent;
import models.Board;
import models.GameState;
import models.Piece;
import models.Position;
import models.enums.PieceState;
import realtime.Motion;
import realtime.RealTimeArbiter;
import rules.PromotionRule;
import rules.WinCondition;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    public static final long JUMP_DURATION = 1000;
    public static final long LONG_REST_DURATION = 1000;
    public static final long SHORT_REST_DURATION = 500;

    private final Board board;
    private final RealTimeArbiter arbiter;
    private final GameState gameState;
    private final List<Motion> activeMotions;
    private final EventBus bus;

    private long gameClockMs = 0;

    public GameEngine(Board board, RealTimeArbiter arbiter, GameState gameState, EventBus bus) {
        this.board = board;
        this.arbiter = arbiter;
        this.gameState = gameState;
        this.bus = bus;
        this.activeMotions = new ArrayList<>();
    }

    public long getGameClockMs() {
        return gameClockMs;
    }

    public void tryMove(Position src, Position dest) {
        if (gameState.isGameOver()) return;
        Piece piece = board.getPieceAt(src);
        if (piece == null) return;
        if (piece.getState() == PieceState.AIRBORNE) return;
        if (isResting(piece)) return;

        MoveResult result = arbiter.validateMove(board, src, dest);
        if (result.isSuccess()) {
            piece.setState(PieceState.AIRBORNE);
            Motion motion = new Motion(piece, src, dest, gameClockMs + result.getTravelTime());
            activeMotions.add(motion);
        }
    }

    public void triggerJump(Position pos) {
        Piece piece = board.getPieceAt(pos);
        if (piece == null) return;
        if (piece.getState() == PieceState.AIRBORNE) return;
        if (isResting(piece)) return;
        piece.setState(PieceState.JUMPING);
        piece.setJumpExpiryTime(gameClockMs + JUMP_DURATION);
        bus.publish("piece.jumped", new JumpEvent(pos));
    }

    public void waitMs(long deltaMs) {
        gameClockMs += deltaMs;
        activeMotions.removeIf(motion -> resolveMotion(motion, gameClockMs));
        revertExpiredTemporaryStates();
    }

    private boolean isResting(Piece piece) {
        boolean inRestState = piece.getState() == PieceState.LONG_RESTING
                || piece.getState() == PieceState.SHORT_RESTING;
        return inRestState && gameClockMs < piece.getRestExpiryTime();
    }

    private void revertExpiredTemporaryStates() {
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                Piece piece = board.getPieceAt(new Position(row, col));
                if (piece == null) continue;

                boolean resting = piece.getState() == PieceState.LONG_RESTING
                        || piece.getState() == PieceState.SHORT_RESTING;
                if (resting && gameClockMs >= piece.getRestExpiryTime()) {
                    piece.setState(PieceState.IDLE);
                }

                if (piece.getState() == PieceState.JUMPING && !piece.isProtectedByJump(gameClockMs)) {
                    // אחרי שהגנת-הקפיצה פגה - קירור-קצר, לא ישר IDLE
                    piece.setState(PieceState.SHORT_RESTING);
                    piece.setRestExpiryTime(gameClockMs + SHORT_REST_DURATION);
                }
            }
        }
    }

    private boolean resolveMotion(Motion motion, long currentClock) {
        if (currentClock < motion.getArrivalTime()) return false;

        Position src = motion.getSource();
        Position dest = motion.getDestination();
        Piece movingPiece = motion.getPiece();

        if (movingPiece == null || board.getPieceAt(src) != movingPiece) {
            return true;
        }

        Piece target = board.getPieceAt(dest);
        if (target != null && target.getColor() != movingPiece.getColor() && target.isProtectedByJump(currentClock)) {
            board.removePiece(src);
            movingPiece.setState(PieceState.CAPTURED);
            return true;
        }

        if (target != null && target.getColor() == movingPiece.getColor()) {
            movingPiece.setState(PieceState.IDLE);
            return true;
        }

        if (target != null) {
            bus.publish("piece.captured", new CaptureEvent(movingPiece, target, src, dest));
            if (WinCondition.isDecisive(target)) {
                gameState.setGameOver(true);
                gameState.setWinner(movingPiece.getColor());
                bus.publish("game.over", new GameOverEvent(movingPiece.getColor()));
            }
            board.removePiece(dest);
        }

        board.movePiece(src, dest);
        movingPiece.setState(PieceState.LONG_RESTING);
        movingPiece.setRestExpiryTime(currentClock + LONG_REST_DURATION);

        bus.publish("move.completed", new MoveEvent(movingPiece, src, dest, target != null));

        if (PromotionRule.isEligible(board, movingPiece, dest)) {
            movingPiece.promote(PromotionRule.promotedType());
        }

        return true;
    }

    public List<Motion> getActiveMotions() {
        return List.copyOf(activeMotions);
    }

    public EventBus getBus() {
        return bus;
    }
}