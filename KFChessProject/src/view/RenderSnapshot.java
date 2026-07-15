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
        List<String> moveLog,
        String whitePlayerName,
        String blackPlayerName,
        List<Position> legalMoves




) {
    public RenderSnapshot {
        pieces = List.copyOf(pieces);
        moveLog = List.copyOf(moveLog);
        legalMoves = List.copyOf(legalMoves);

    }
}