package io;

import models.Board;
import models.GameSnapshot;
import models.Piece;
import models.Position;
import models.enums.PieceType;

public class BoardPrinter {

    /**
     * מקבל תמונת מצב של המשחק ומחזיר מחרוזת טקסט שמייצגת את הלוח.
     */
    public static String print(GameSnapshot snapshot) {
        if (snapshot == null) return "";

        Board board = snapshot.getBoard();
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);

                if (piece == null) {
                    sb.append(".");
                } else {
                    String colorChar = piece.getColor().name().substring(0, 1).toLowerCase();

                    String kindChar;
                    PieceType type = piece.getType();
                    if (type == PieceType.KING)   kindChar = "K";
                    else if (type == PieceType.QUEEN)  kindChar = "Q";
                    else if (type == PieceType.ROOK)   kindChar = "R";
                    else if (type == PieceType.BISHOP) kindChar = "B";
                    else if (type == PieceType.KNIGHT) kindChar = "N";
                    else if (type == PieceType.PAWN)   kindChar = "P";
                    else kindChar = "?";

                    sb.append(colorChar).append(kindChar);
                }

                if (col < board.getWidth() - 1) {
                    sb.append(" ");
                }
            }
            if (row < board.getHeight() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}