package io;

import models.Board;
import models.GameSnapshot;
import models.Piece;
import models.Position;

public class BoardPrinter {

    /**
     * מקבל תמונת מצב של המשחק ומחזיר מחרוזת טקסט שמייצגת את הלוח ומצב המשחק.
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
                    String kindChar = piece.getType().name().substring(0, 1).toUpperCase();
                    sb.append(colorChar).append(kindChar);
                }

                if (col < board.getWidth() - 1) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }

        if (snapshot.getGameState().isGameOver()) {
            sb.append("Game Over! Winner: ").append(snapshot.getGameState().getWinner());
        } else {
            sb.append("Game is active");
        }

        return sb.toString();
    }
}
