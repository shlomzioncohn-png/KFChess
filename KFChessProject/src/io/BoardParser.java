package io;

import models.Board;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;

public class BoardParser {

    private static final String VALID_PIECE_LETTERS = "KQRBNP";

    /**
     * מקבל מחרוזת טקסט שמייצגת לוח, ומפענח אותה לאובייקט Board מלא בכלים.
     */
    public static Board parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be empty");
        }

        String[] lines = rawText.trim().split("\n");
        int height = lines.length;

        String[][] tokenRows = new String[height][];
        int width = -1;

        for (int r = 0; r < height; r++) {
            String[] tokens = lines[r].trim().split("\\s+");
            if (width == -1) {
                width = tokens.length;
            } else if (tokens.length != width) {
                throw new IllegalArgumentException("ROW_WIDTH_MISMATCH");
            }
            tokenRows[r] = tokens;
        }
        for (String[] row : tokenRows) {
            for (String token : row) {
                if (!isValidToken(token)) {
                    throw new IllegalArgumentException("UNKNOWN_TOKEN");
                }
            }
        }

        Board board = new MatrixBoard(width, height);
        int pieceCounter = 1;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                String token = tokenRows[r][c];

                if (token.equals(".")) {
                    continue;
                }
                char colorChar = token.charAt(0);
                char kindChar = token.charAt(1);

                PieceColor color = (colorChar == 'w') ? PieceColor.WHITE : PieceColor.BLACK;

                PieceType kind;
                switch (kindChar) {
                    case 'K': kind = PieceType.KING; break;
                    case 'Q': kind = PieceType.QUEEN; break;
                    case 'R': kind = PieceType.ROOK; break;
                    case 'B': kind = PieceType.BISHOP; break;
                    case 'N': kind = PieceType.KNIGHT; break;
                    case 'P': kind = PieceType.PAWN; break;
                    default:
                        throw new IllegalArgumentException("UNKNOWN_TOKEN");
                }
                Position pos = new Position(r, c);
                String uniqueId = "piece_" + pieceCounter++;
                Piece piece = new Piece(uniqueId, color, kind, pos);
                board.addPiece(pos, piece);
            }
        }

        return board;
    }

    private static boolean isValidToken(String token) {
        if (token.equals(".")) {
            return true;
        }
        if (token.length() != 2) {
            return false;
        }
        char color = token.charAt(0);
        char piece = token.charAt(1);
        return (color == 'w' || color == 'b') && VALID_PIECE_LETTERS.indexOf(piece) != -1;
    }
}
