package view;

import models.enums.PieceColor;
import models.enums.PieceType;

public record PieceRenderSnapshot(
        String id,
        PieceType type,
        PieceColor color,
        String stateFolder,   // "idle" / "move" / "jump" / "short_rest" / "long_rest"
        double pixelX,
        double pixelY,
        long stateElapsedMillis
) {}