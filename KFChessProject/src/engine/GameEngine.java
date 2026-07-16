package engine;

import models.Board;
import models.GameState;
import models.Piece;
import models.Position;
import models.enums.PieceState;
import realtime.Motion;
import realtime.RealTimeArbiter;
import rules.PieceValue;
import rules.PromotionRule;
import rules.WinCondition;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    public static final long JUMP_DURATION = 1000;
    public static final long LONG_REST_DURATION = 1000;   // קירור אחרי תזוזה רגילה
    public static final long SHORT_REST_DURATION = 500;   // קירור אחרי קפיצה

    private final Board board;
    private final RealTimeArbiter arbiter;
    private final GameState gameState;
    private final List<Motion> activeMotions;

    private long gameClockMs = 0;

    public GameEngine(Board board, RealTimeArbiter arbiter, GameState gameState) {
        this.board = board;
        this.arbiter = arbiter;
        this.gameState = gameState;
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
        if (isResting(piece)) return;                          // <-- חדש: חסימה בזמן-קירור

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
        if (isResting(piece)) return;                          // <-- חדש: חסימה בזמן-קירור

        piece.setState(PieceState.JUMPING);
        piece.setJumpExpiryTime(gameClockMs + JUMP_DURATION);
    }

    public void waitMs(long deltaMs) {
        gameClockMs += deltaMs;
        activeMotions.removeIf(motion -> resolveMotion(motion, gameClockMs));
        revertExpiredTemporaryStates();                         // <-- חדש: בדיקה תקופתית
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
                    piece.setState(PieceState.IDLE);
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
            gameState.addScore(movingPiece.getColor(), PieceValue.getValue(target.getType()));
            if (WinCondition.isDecisive(target)) {
                gameState.setGameOver(true);
                gameState.setWinner(movingPiece.getColor());
            }
            board.removePiece(dest);
        }

        board.movePiece(src, dest);
        movingPiece.setState(PieceState.LONG_RESTING);                        // <-- שונה מ-IDLE
        movingPiece.setRestExpiryTime(currentClock + LONG_REST_DURATION);     // <-- חדש

        String logEntry = movingPiece.getColor() + " " + movingPiece.getType() + " " + src + " -> " + dest
                + (target != null ? " (captured " + target.getType() + ")" : "");
        gameState.addLogEntry(logEntry);

        if (PromotionRule.isEligible(board, movingPiece, dest)) {
            movingPiece.promote(PromotionRule.promotedType());
        }

        return true;
    }

    public List<Motion> getActiveMotions() {
        return List.copyOf(activeMotions);
    }
}