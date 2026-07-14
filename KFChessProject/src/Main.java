
import engine.GameEngine;
import io.BoardParser;
import io.BoardPrinter;
import models.Board;
import models.GameState;
import realtime.RealTimeArbiter;
import input.Controller;
import models.GameSnapshot;
import view.Renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        List<String> boardLines = readBoardSection(reader);
        if (boardLines.isEmpty()) {
            return;
        }

        Board board;
        try {
            board = BoardParser.parse(String.join("\n", boardLines));
            Renderer renderer = new Renderer();
            renderer.renderFromRealBoard(board);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR " + e.getMessage());
            return;
        }

        GameState gameState = new GameState();
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameEngine engine = new GameEngine(board, arbiter, gameState);
        Controller controller = new Controller(engine, board);

        long clock = 0L;
        String line;

        while ((line = reader.readLine()) != null) {
            String cmd = line.trim();
            if (cmd.isEmpty()) {
                continue;
            }

            String[] parts = cmd.split("\\s+");
            String op = parts[0].toLowerCase();

            if (op.equals("click") && parts.length == 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                controller.handleMouseClick(x, y, clock);

            } else if (op.equals("wait") && parts.length == 2) {
                long ms = Long.parseLong(parts[1]);
                clock += ms;
                controller.update(clock);

            } else if (op.equals("jump") && parts.length == 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                controller.handleJumpCommand(x, y, clock);

            } else if (op.equals("print") && parts.length == 2 && parts[1].equalsIgnoreCase("board")) {
                GameSnapshot snapshot = new GameSnapshot(board, gameState);
                System.out.println(BoardPrinter.print(snapshot));
            }
        }
    }

    private static List<String> readBoardSection(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        boolean readingBoard = false;
        String line;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("Board:")) {
                readingBoard = true;
                continue;
            }
            if (trimmed.equalsIgnoreCase("Commands:") || (trimmed.isEmpty() && readingBoard)) {
                break;
            }
            if (readingBoard) {
                lines.add(line);
            }
        }
        return lines;
    }
}