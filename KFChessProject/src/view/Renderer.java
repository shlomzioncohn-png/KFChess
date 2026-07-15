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
        gameWindow.update(canvas);
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