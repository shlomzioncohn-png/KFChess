package view;

import models.Position;

import java.util.List;

public record RenderSnapshot(
        int boardWidth,
        int boardHeight,
        List<PieceRenderSnapshot> pieces,
        Position selectedPosition,
        boolean gameOver
) {
    public RenderSnapshot {
        pieces = List.copyOf(pieces);
    }
}