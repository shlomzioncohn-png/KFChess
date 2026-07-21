package client;

import models.Position;

public class ServerMessageParser {

    public static Position[] parseMove(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 3 || !parts[0].equals("MOVE")) {
            throw new IllegalArgumentException("NOT_A_MOVE_MESSAGE");
        }

        Position src = parseCoords(parts[1]);
        Position dest = parseCoords(parts[2]);

        return new Position[] { src, dest };
    }

    public static Position parseJump(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 2 || !parts[0].equals("JUMP")) {
            throw new IllegalArgumentException("NOT_A_JUMP_MESSAGE");
        }
        return parseCoords(parts[1]);
    }

    private static Position parseCoords(String coords) {
        String[] rc = coords.split(",");
        int row = Integer.parseInt(rc[0]);
        int col = Integer.parseInt(rc[1]);
        return new Position(row, col);
    }
}