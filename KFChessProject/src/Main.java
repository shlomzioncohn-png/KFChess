import engine.GameEngine;
import io.BoardParser;
import io.BoardPrinter;
import models.Board;
import models.GameState;
import realtime.RealTimeArbiter;
import input.Controller;
import input.GameClickHandler;
import view.FrameRenderer;
import view.Renderer;
import view.RenderSnapshot;
import view.SnapshotFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int CELL_SIZE = 100;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        List<String> boardLines = readBoardSection(reader);
        if (boardLines.isEmpty()) {
            return;
        }

        long clock = 0L;
        Board board;
        RealTimeArbiter arbiter;
        GameState gameState;
        GameEngine engine;
        Controller controller;
        Renderer renderer;
        FrameRenderer frameRenderer;

        try {
            board = BoardParser.parse(String.join("\n", boardLines));
            arbiter = new RealTimeArbiter();
            gameState = new GameState();
            engine = new GameEngine(board, arbiter, gameState);
            controller = new Controller(engine, board);

            renderer = new Renderer("resources/pieces1", CELL_SIZE);
            renderer.initWindow(board.getWidth(), board.getHeight());

            frameRenderer = clockValue -> {
                RenderSnapshot snap = SnapshotFactory.build(
                        board, engine, arbiter, controller.getSelectedPosition(), CELL_SIZE, clockValue);
                renderer.renderFrame(snap);
            };

            RenderSnapshot initialSnapshot = SnapshotFactory.build(
                    board, engine, arbiter, controller.getSelectedPosition(), CELL_SIZE, clock);
            renderer.renderFrame(initialSnapshot);

        } catch (IllegalArgumentException e) {
            System.out.println("ERROR " + e.getMessage());
            return;
        }

        GameClickHandler clickHandler = new GameClickHandler(controller, frameRenderer);
        renderer.setOnClick(clickHandler);

        String line;

        while ((line = reader.readLine()) != null) {
            String cmd = line.trim();
            if (cmd.isEmpty()) continue;

            String[] parts = cmd.split("\\s+");
            String op = parts[0].toLowerCase();

            if (op.equals("click") && parts.length == 3) {
                controller.handleMouseClick(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), clock);

            } else if (op.equals("wait") && parts.length == 2) {
                clock += Long.parseLong(parts[1]);
                controller.update(clock);
                clickHandler.setClock(clock);

                RenderSnapshot snapshot = SnapshotFactory.build(
                        board, engine, arbiter, controller.getSelectedPosition(), CELL_SIZE, clock);
                renderer.renderFrame(snapshot);

            } else if (op.equals("jump") && parts.length == 3) {
                controller.handleJumpCommand(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), clock);

            } else if (op.equals("print") && parts.length == 2 && parts[1].equalsIgnoreCase("board")) {
                models.GameSnapshot printSnapshot = new models.GameSnapshot(board, gameState);
                System.out.println(BoardPrinter.print(printSnapshot));
            }
        }
    }

    private static List<String> readBoardSection(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        boolean readingBoard = false;
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("Board:")) { readingBoard = true; continue; }
            if (trimmed.equalsIgnoreCase("Commands:") || (trimmed.isEmpty() && readingBoard)) break;
            if (readingBoard) lines.add(line);
        }
        return lines;
    }
}