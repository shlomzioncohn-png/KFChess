package view;

import models.Position;

import java.awt.Color;
import java.awt.Dimension;

public class Renderer {

    private static final String BOARD_IMAGE_PATH = "resources/board.png";
    private static final int SIDEBAR_WIDTH = 250;

    private final GameWindow gameWindow = new GameWindow();
    private final PieceImageLoader imageLoader;
    private final int cellSize;

    public Renderer(String piecesRootFolder, int cellSize) {
        this.cellSize = cellSize;
        this.imageLoader = new PieceImageLoader(piecesRootFolder, cellSize);
    }

    public void initWindow(int boardWidth, int boardHeight) {
        Img canvas = createCanvas(boardWidth * cellSize, boardHeight * cellSize);
        gameWindow.open(canvas);
    }

    public void renderFrame(RenderSnapshot snapshot) {
        Img canvas = createCanvas(
                snapshot.boardWidth() * cellSize,
                snapshot.boardHeight() * cellSize
        );
        drawLegalMoves(canvas, snapshot);

        for (PieceRenderSnapshot piece : snapshot.pieces()) {
            Img pieceImage = imageLoader.getFrame(piece);
            pieceImage.drawOn(canvas, (int) Math.round(piece.pixelX()), (int) Math.round(piece.pixelY()));
        }

        drawSelectedCell(canvas, snapshot);
        drawScore(canvas, snapshot);
        drawMoveLog(canvas, snapshot);

        if (snapshot.gameOver()) {
            drawGameOver(canvas, snapshot);
        }

        gameWindow.update(canvas);
    }

    private Img createCanvas(int boardPixelWidth, int boardPixelHeight) {
        Img canvas = Img.blank(boardPixelWidth + SIDEBAR_WIDTH, boardPixelHeight, Color.WHITE);

        Img boardImg = new Img().read(BOARD_IMAGE_PATH,
                new Dimension(boardPixelWidth, boardPixelHeight), false, null);
        boardImg.drawOn(canvas, 0, 0);

        canvas.fillRect(boardPixelWidth, 0, SIDEBAR_WIDTH, boardPixelHeight, new Color(240, 240, 240));

        return canvas;
    }

    private void drawSelectedCell(Img canvas, RenderSnapshot snapshot) {
        if (snapshot.selectedPosition() == null) return;
        int x = snapshot.selectedPosition().getCol() * cellSize;
        int y = snapshot.selectedPosition().getRow() * cellSize;
        canvas.drawRect(x, y, cellSize, cellSize, new Color(255, 215, 0, 180), 4);
    }

    private void drawScore(Img canvas, RenderSnapshot snapshot) {
        int x = snapshot.boardWidth() * cellSize + 15;
        canvas.putText("Score", x, 30, 1.4f, Color.DARK_GRAY, 2);
        canvas.putText(snapshot.whitePlayerName() + ": " + snapshot.whiteScore(), x, 60, 1.1f, Color.BLACK, 1);
        canvas.putText(snapshot.blackPlayerName() + ": " + snapshot.blackScore(), x, 85, 1.1f, Color.BLACK, 1);
    }

    private void drawMoveLog(Img canvas, RenderSnapshot snapshot) {
        int x = snapshot.boardWidth() * cellSize + 15;
        int startY = 130;
        canvas.putText("Move Log", x, startY, 1.2f, Color.DARK_GRAY, 2);

        for (int i = 0; i < snapshot.moveLog().size(); i++) {
            canvas.putText(snapshot.moveLog().get(i), x, startY + 25 + i * 18, 0.75f, Color.BLUE, 1);
        }
    }

    private void drawGameOver(Img canvas, RenderSnapshot snapshot) {
        String text = snapshot.winner() == null ? "Game Over" : snapshot.winner() + " wins";
        canvas.putText(text, 40, 60, 2.0f, Color.RED, 2);
    }

    public void setOnClick(ClickListener listener) {
        gameWindow.setClickListener(listener);
    }

    private void drawLegalMoves(Img canvas, RenderSnapshot snapshot) {
        for (Position pos : snapshot.legalMoves()) {
            int x = pos.getCol() * cellSize;
            int y = pos.getRow() * cellSize;
            canvas.fillRect(x, y, cellSize, cellSize, new Color(0, 200, 0, 90));
        }
    }
}