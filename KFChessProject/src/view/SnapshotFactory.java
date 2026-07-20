package view;

import engine.GameEngine;
import models.Board;
import models.GameState;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceState;
import realtime.Motion;
import realtime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.List;

public class SnapshotFactory {

    private static final double JUMP_HEIGHT_PX = 40.0;

    public static RenderSnapshot build(Board board, GameEngine engine,
                                       RealTimeArbiter arbiter,
                                       Position selectedPosition,
                                       int cellSize,
                                       GameState gameState,
                                       String whiteName,
                                       String blackName) {
        long currentClock = engine.getGameClockMs();
        List<PieceRenderSnapshot> pieceSnapshots = new ArrayList<>();
        List<Motion> activeMotions = engine.getActiveMotions();

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                Piece piece = board.getPieceAt(new Position(row, col));
                if (piece == null || piece.getState() == PieceState.CAPTURED) {
                    continue;
                }
                pieceSnapshots.add(buildPieceSnapshot(piece, activeMotions, arbiter, cellSize, currentClock));
            }
        }
        String winnerText = gameState.isGameOver() && gameState.getWinner() != null
                ? gameState.getWinner().name()
                : null;

        List<String> fullLog = gameState.getMoveLog();
        List<String> recentLog = fullLog.size() <= 5
                ? fullLog
                : fullLog.subList(fullLog.size() - 5, fullLog.size());

        List<Position> legalMoves = selectedPosition != null
                ? new rules.RuleEngine().getLegalDestinations(board, selectedPosition)
                : List.of();

        return new RenderSnapshot(
                board.getWidth(), board.getHeight(),
                pieceSnapshots, selectedPosition,
                gameState.isGameOver(), winnerText,
                gameState.getScore(PieceColor.WHITE),
                gameState.getScore(PieceColor.BLACK),
                recentLog,
                whiteName,
                blackName,
                legalMoves
        );
    }

    private static PieceRenderSnapshot buildPieceSnapshot(Piece piece, List<Motion> activeMotions,
                                                          RealTimeArbiter arbiter,
                                                          int cellSize, long currentClock) {
        String stateFolder;
        double pixelX;
        double pixelY;
        long stateElapsedMillis;

        if (piece.getState() == PieceState.AIRBORNE) {
            Motion motion = findMotionFor(piece, activeMotions);
            if (motion != null) {
                long duration = arbiter.calculateTravelTime(motion.getSource(), motion.getDestination());
                long remaining = motion.getArrivalTime() - currentClock;
                long elapsed = Math.max(0, duration - remaining);
                double progress = duration == 0 ? 1.0 : Math.min(1.0, elapsed / (double) duration);

                double startX = motion.getSource().getCol() * cellSize;
                double startY = motion.getSource().getRow() * cellSize;
                double endX = motion.getDestination().getCol() * cellSize;
                double endY = motion.getDestination().getRow() * cellSize;

                pixelX = startX + (endX - startX) * progress;
                pixelY = startY + (endY - startY) * progress;
                stateFolder = "move";
                stateElapsedMillis = elapsed;
            } else {
                pixelX = piece.getCell().getCol() * cellSize;
                pixelY = piece.getCell().getRow() * cellSize;
                stateFolder = "idle";
                stateElapsedMillis = currentClock;
            }
        } else {
            pixelX = piece.getCell().getCol() * cellSize;
            pixelY = piece.getCell().getRow() * cellSize;

            switch (piece.getState()) {
                case JUMPING -> {
                    stateFolder = "jump";
                    stateElapsedMillis = Math.max(0,
                            GameEngine.JUMP_DURATION - (piece.getJumpExpiryTime() - currentClock));

                    double jumpProgress = Math.min(1.0, stateElapsedMillis / (double) GameEngine.JUMP_DURATION);
                    double jumpOffset = -JUMP_HEIGHT_PX * 4 * jumpProgress * (1 - jumpProgress);
                    pixelY = pixelY + jumpOffset;
                }
                case LONG_RESTING -> {
                    stateFolder = "long_rest";
                    stateElapsedMillis = Math.max(0,
                            GameEngine.LONG_REST_DURATION - (piece.getRestExpiryTime() - currentClock));
                }
                case SHORT_RESTING -> {
                    stateFolder = "short_rest";
                    stateElapsedMillis = Math.max(0,
                            GameEngine.SHORT_REST_DURATION - (piece.getRestExpiryTime() - currentClock));
                }
                default -> {
                    stateFolder = "idle";
                    stateElapsedMillis = currentClock;
                }
            }
        }

        return new PieceRenderSnapshot(
                piece.getId(), piece.getType(), piece.getColor(),
                stateFolder, pixelX, pixelY, stateElapsedMillis
        );
    }

    private static Motion findMotionFor(Piece piece, List<Motion> activeMotions) {
        for (Motion motion : activeMotions) {
            if (motion.getPiece() == piece) {
                return motion;
            }
        }
        return null;
    }
}