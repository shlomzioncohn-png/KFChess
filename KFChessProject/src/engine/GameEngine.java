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

    private final Board board;
    private final RealTimeArbiter arbiter;
    private final GameState gameState;
    private final List<Motion> activeMotions;

    private long gameClockMs = 0;   // <-- חדש: השעון חי כאן, לא בחוץ

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
        piece.setState(PieceState.JUMPING);
        piece.setJumpExpiryTime(gameClockMs + JUMP_DURATION);
    }

    public void waitMs(long deltaMs) {
        gameClockMs += deltaMs;
        activeMotions.removeIf(motion -> resolveMotion(motion, gameClockMs));
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
        movingPiece.setState(PieceState.IDLE);

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