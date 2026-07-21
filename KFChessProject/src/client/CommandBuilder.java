package client;

import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;

public class CommandBuilder {

    public static String buildMoveCommand(Piece piece, Position src, Position dest, int boardHeight) {
        char colorChar = piece.getColor() == PieceColor.WHITE ? 'W' : 'B';
        char typeChar = typeToChar(piece.getType());

        return "" + colorChar + typeChar
                + positionToAlgebraic(src, boardHeight)
                + positionToAlgebraic(dest, boardHeight);
    }

    public static String buildJumpCommand(Piece piece, Position pos, int boardHeight) {
        char colorChar = piece.getColor() == PieceColor.WHITE ? 'W' : 'B';
        return "" + colorChar + "J" + positionToAlgebraic(pos, boardHeight);
    }

    private static String positionToAlgebraic(Position pos, int boardHeight) {
        char file = (char) ('a' + pos.getCol());
        int rank = boardHeight - pos.getRow();
        return "" + file + rank;
    }

    private static char typeToChar(PieceType type) {
        return switch (type) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case ROOK -> 'R';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case PAWN -> 'P';
        };
    }
}