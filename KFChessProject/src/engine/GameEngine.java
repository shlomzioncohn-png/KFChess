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

    public  static final long JUMP_DURATION = 1000;

    private final GameState gameState;
    private final Board board;
    private final RealTimeArbiter arbiter;
    private final List<Motion> activeMotions;


    public GameEngine(Board board, RealTimeArbiter arbiter,GameState gameState) {
        this.board = board;
        this.arbiter = arbiter;
        this.activeMotions = new ArrayList<>();
        this.gameState = gameState;
    }

    public void tryMove(Position src, Position dest, long currentClock) {

        if (gameState.isGameOver()) {
            return;
        }

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
                    piece,
                    src,
                    dest,
                    currentClock + result.getTravelTime()
            );

            activeMotions.add(motion);
        }
    }


    public void triggerJump(Position pos, long currentClock) {
        if (gameState.isGameOver()) {
            return;
        }

        Piece piece = board.getPieceAt(pos);
        if (piece == null || piece.getState() == PieceState.AIRBORNE) {
            return;
        }

        piece.setState(PieceState.JUMPING);
        piece.setJumpExpiryTime(currentClock + JUMP_DURATION);
    }

    public void update(long currentClock) {
        activeMotions.removeIf(motion -> resolveMotion(motion, currentClock));
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
            return true; // ביטול ההעברה כי זה לא חוקי
        }

        // 3. תפיסה רגילה: הסרת היעד והעברת הכלי
        if (target != null) {
            gameState.addScore(movingPiece.getColor(), PieceValue.getValue(target.getType()));  // <-- חדש

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

    public List<realtime.Motion> getActiveMotions() {
        return List.copyOf(activeMotions);
    }
}