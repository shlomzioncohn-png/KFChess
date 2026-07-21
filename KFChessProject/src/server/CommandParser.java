package server;

import models.Board;
import models.Position;

public class CommandParser {

    public static Position[] parseMove(String command, Board board) {
        if (command == null || command.length() != 6) {
            throw new IllegalArgumentException("BAD_COMMAND_FORMAT");
        }

        String srcText = command.substring(2, 4);
        String destText = command.substring(4, 6);

        Position src = algebraicToPosition(srcText, board.getHeight());
        Position dest = algebraicToPosition(destText, board.getHeight());

        return new Position[] { src, dest };
    }

    public static Position parseJump(String command, Board board) {
        if (command == null || command.length() != 4) {
            throw new IllegalArgumentException("BAD_COMMAND_FORMAT");
        }
        String posText = command.substring(2, 4);
        return algebraicToPosition(posText, board.getHeight());
    }

    private static Position algebraicToPosition(String algebraic, int boardHeight) {
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);

        int col = fileChar - 'a';
        int rank = rankChar - '0';
        int row = boardHeight - rank;

        return new Position(row, col);
    }
}