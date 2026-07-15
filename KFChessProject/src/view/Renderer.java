package view;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

public class Renderer {

    private static final String BOARD_IMAGE_PATH = "resources/board.png";

    private final GameWindow gameWindow = new GameWindow();
    private final PieceImageLoader imageLoader;
    private final int cellSize;

    public Renderer(String piecesRootFolder, int cellSize) {
        this.cellSize = cellSize;
        this.imageLoader = new PieceImageLoader(piecesRootFolder, cellSize);
    }

    public void initWindow(int boardWidth, int boardHeight) {
        gameWindow.open(loadBoardImage(boardWidth, boardHeight));
    }

    public void renderFrame(RenderSnapshot snapshot) {
        Img canvas = loadBoardImage(snapshot.boardWidth(), snapshot.boardHeight());

        for (PieceRenderSnapshot piece : snapshot.pieces()) {
            Img pieceImage = imageLoader.getFrame(piece);
            pieceImage.drawOn(canvas, (int) Math.round(piece.pixelX()), (int) Math.round(piece.pixelY()));
        }

        drawSelectedCell(canvas, snapshot);
        drawScore(canvas, snapshot);
        drawPlayerNames(canvas, snapshot);
        drawMoveLog(canvas, snapshot);

        if (snapshot.gameOver()) {
            drawGameOver(canvas, snapshot);
        }

        gameWindow.update(canvas);
    }

    private void drawMoveLog(Img canvas, RenderSnapshot snapshot) {
        int startY = 20;
        int lineHeight = 16;
        int x = canvas.get().getWidth() - 260;

        for (int i = 0; i < snapshot.moveLog().size(); i++) {
            canvas.putText(snapshot.moveLog().get(i), x, startY + i * lineHeight, 0.8f, Color.BLUE, 1);
        }
    }

    private void drawPlayerNames(Img canvas, RenderSnapshot snapshot) {
        canvas.putText(snapshot.whitePlayerName(), 10, canvas.get().getHeight() - 60, 1.2f, Color.BLACK, 1);
        canvas.putText(snapshot.blackPlayerName(), 10, canvas.get().getHeight() - 80, 1.2f, Color.BLACK, 1);
    }

    private void drawScore(Img canvas, RenderSnapshot snapshot) {
        canvas.putText("White: " + snapshot.whiteScore(), 10, canvas.get().getHeight() - 40, 1.2f, Color.RED, 1);
        canvas.putText("Black: " + snapshot.blackScore(), 10, canvas.get().getHeight() - 20, 1.2f, Color.RED, 1);
    }

    private void drawGameOver(Img canvas, RenderSnapshot snapshot) {
        String text = snapshot.winner() == null ? "Game Over" : snapshot.winner() + " wins";
        canvas.putText(text, 40, 60, 2.0f, Color.RED, 2);
    }

    private void drawSelectedCell(Img canvas, RenderSnapshot snapshot) {
        if (snapshot.selectedPosition() == null) return;
        int x = snapshot.selectedPosition().getCol() * cellSize;
        int y = snapshot.selectedPosition().getRow() * cellSize;
        canvas.drawRect(x, y, cellSize, cellSize, new Color(255, 215, 0, 180), 4);
    }

    private Img loadBoardImage(int boardWidth, int boardHeight) {
        return new Img().read(BOARD_IMAGE_PATH,
                new Dimension(boardWidth * cellSize, boardHeight * cellSize), false, null);
    }

    public void setOnClick(ClickListener listener) {
        gameWindow.setClickListener(listener);
    }
}