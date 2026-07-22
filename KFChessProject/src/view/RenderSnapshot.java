package view;

import models.Position;

import java.util.List;

public record RenderSnapshot(
        int boardWidth,
        int boardHeight,
        List<PieceRenderSnapshot> pieces,
        Position selectedPosition,
        boolean gameOver,
        String winner,
        int whiteScore,
        int blackScore,
        List<MoveLogRow> whiteMoveLog,
        List<MoveLogRow> blackMoveLog,
        String whitePlayerName,
        String blackPlayerName,
        List<Position> legalMoves,
        Integer disconnectSecondsLeft,
        Integer disconnectTotalSeconds,
        Integer returnCountdownSecondsLeft,
        Integer returnCountdownTotalSeconds,
        String roomId

) {
    public RenderSnapshot {
        pieces = List.copyOf(pieces);
        whiteMoveLog = List.copyOf(whiteMoveLog);
        blackMoveLog = List.copyOf(blackMoveLog);
        legalMoves = List.copyOf(legalMoves);

    }
}