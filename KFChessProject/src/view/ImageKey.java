package view;

import models.enums.PieceColor;
import models.enums.PieceType;

public record ImageKey(PieceType type, PieceColor color, String stateFolder) {}