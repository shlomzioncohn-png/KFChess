package view;

import engine.GameEngine;
import models.Board;
import models.Piece;
import models.Position;
import models.enums.PieceState;
import realtime.Motion;
import realtime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.List;

public class SnapshotFactory {

    public static RenderSnapshot build(Board board, GameEngine engine,
                                       RealTimeArbiter arbiter,
                                       Position selectedPosition,
                                       int cellSize, long currentClock) {

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

        return new RenderSnapshot(
                board.getWidth(), board.getHeight(),
                pieceSnapshots, selectedPosition, false
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
                // גיבוי: לא נמצאה תנועה תואמת (לא צפוי, אבל לא קורסים)
                pixelX = piece.getCell().getCol() * cellSize;
                pixelY = piece.getCell().getRow() * cellSize;
                stateFolder = "idle";
                stateElapsedMillis = currentClock;
            }
        } else {
            pixelX = piece.getCell().getCol() * cellSize;
            pixelY = piece.getCell().getRow() * cellSize;

            if (piece.getState() == PieceState.JUMPING) {
                stateFolder = "jump";
                stateElapsedMillis = Math.max(0,
                        GameEngine.JUMP_DURATION - (piece.getJumpExpiryTime() - currentClock));
            } else {
                stateFolder = "idle";
                stateElapsedMillis = currentClock; // ל-idle בלולאה זה מספיק
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