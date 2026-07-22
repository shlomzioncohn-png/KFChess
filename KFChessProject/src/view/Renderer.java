package view;

import models.Position;

import java.awt.Color;
import java.awt.Dimension;

public class Renderer {

    private static final String BOARD_IMAGE_PATH = "resources/board_classic.png";
    private static final int SIDEBAR_WIDTH = 250;
    private static final int MIN_CELL_SIZE = 20;

    private final GameWindow gameWindow = new GameWindow();
    private final PieceImageLoader imageLoader;

    // נקרא (בדרך כלל) מ-render loop, אבל resize מגיע מה-EDT - חייב volatile, אין הבטחה ששניהם על אותו thread
    private volatile int cellSize;

    public Renderer(String piecesRootFolder, int cellSize) {
        this.cellSize = cellSize;
        this.imageLoader = new PieceImageLoader(piecesRootFolder, cellSize);
    }

    public int getCellSize() {
        return cellSize;
    }

    public void setOnResize(Runnable listener) {
        gameWindow.setResizeListener(listener);
    }

    public void initWindow(int boardWidth, int boardHeight) {
        Img canvas = createCanvas(boardWidth * cellSize, boardHeight * cellSize);
        Dimension minimumSize = new Dimension(
                boardWidth * MIN_CELL_SIZE + SIDEBAR_WIDTH,
                boardHeight * MIN_CELL_SIZE
        );
        gameWindow.open(canvas, minimumSize);
    }

    public void renderFrame(RenderSnapshot snapshot) {
        updateCellSize(snapshot.boardWidth(), snapshot.boardHeight());

        Img canvas = createCanvas(
                snapshot.boardWidth() * cellSize,
                snapshot.boardHeight() * cellSize
        );
        drawLegalMoves(canvas, snapshot);
        drawRestingIndicators(canvas, snapshot);   // <-- חדש


        for (PieceRenderSnapshot piece : snapshot.pieces()) {
            Img pieceImage = imageLoader.getFrame(piece);
            pieceImage.drawOn(canvas, (int) Math.round(piece.pixelX()), (int) Math.round(piece.pixelY()),
                    cellSize, cellSize);
        }

        drawSelectedCell(canvas, snapshot);

        if (snapshot.roomId() != null) {
            drawRoomId(canvas, snapshot);
        }

        int boardPixelHeight = snapshot.boardHeight() * cellSize;
        int halfHeight = boardPixelHeight / 2;
        drawPlayerPanel(canvas, snapshot, true, 0, halfHeight);
        drawPlayerPanel(canvas, snapshot, false, halfHeight, boardPixelHeight - halfHeight);
        canvas.fillRect(snapshot.boardWidth() * cellSize, halfHeight, SIDEBAR_WIDTH, 2, Color.DARK_GRAY);

        if (snapshot.disconnectSecondsLeft() != null) {
            drawDisconnectCountdown(canvas, snapshot);
        }

        if (snapshot.gameOver()) {
            drawGameOver(canvas, snapshot);
        }

        gameWindow.update(canvas);
    }

    // מחשבת מחדש את cellSize לפי הגודל הזמין בפועל בחלון, כדי שהלוח יישאר מרובע ולא יימתח.
    // ה-sidebar נשאר ברוחב קבוע (SIDEBAR_WIDTH) - רק אזור הלוח עצמו מתאים את עצמו.
    private void updateCellSize(int boardWidthCells, int boardHeightCells) {
        Dimension available = gameWindow.getContentSize();
        if (available.width <= 0 || available.height <= 0) {
            return; // החלון עוד לא גלוי/מוצג בפועל - משאירים את הערך האחרון הידוע
        }

        int availableBoardWidth = available.width - SIDEBAR_WIDTH;
        int availableBoardHeight = available.height;

        int computed = Math.min(availableBoardWidth / boardWidthCells, availableBoardHeight / boardHeightCells);
        cellSize = Math.max(MIN_CELL_SIZE, computed);
    }

    private Img createCanvas(int boardPixelWidth, int boardPixelHeight) {
        Img canvas = Img.blank(boardPixelWidth + SIDEBAR_WIDTH, boardPixelHeight, Color.WHITE);

        Img boardImg = new Img().read(BOARD_IMAGE_PATH,
                new Dimension(boardPixelWidth, boardPixelHeight), false, null);
        boardImg.drawOn(canvas, 0, 0);

        canvas.fillRect(boardPixelWidth, 0, SIDEBAR_WIDTH, boardPixelHeight, new Color(240, 240, 240));

        return canvas;
    }

    // באנר קבוע בראש הלוח, רק ב-room ידני (Create/Join) - לא במשחקי Quick Play אנונימיים
    private void drawRoomId(Img canvas, RenderSnapshot snapshot) {
        String text = "Room: " + snapshot.roomId();
        int boardPixelWidth = snapshot.boardWidth() * cellSize;

        canvas.fillRect(0, 0, boardPixelWidth, 22, new Color(0, 0, 0, 160));
        canvas.putText(text, 10, 16, 1.0f, Color.WHITE, 1);
    }

    private void drawSelectedCell(Img canvas, RenderSnapshot snapshot) {
        if (snapshot.selectedPosition() == null) return;
        int x = snapshot.selectedPosition().getCol() * cellSize;
        int y = snapshot.selectedPosition().getRow() * cellSize;
        canvas.drawRect(x, y, cellSize, cellSize, new Color(255, 215, 0, 180), 4);
    }

    // מצייר את הפאנל של שחקן אחד (צבע+שם+ניקוד, ואז טבלת המהלכים שלו בלבד) בתוך פלח גובה נתון בסיידבר
    private void drawPlayerPanel(Img canvas, RenderSnapshot snapshot, boolean isWhite, int panelY, int panelHeight) {
        int x = snapshot.boardWidth() * cellSize + 15;

        Color swatchColor = isWhite ? Color.WHITE : Color.BLACK;
        String colorLabel = isWhite ? "WHITE" : "BLACK";
        String playerName = isWhite ? snapshot.whitePlayerName() : snapshot.blackPlayerName();
        int score = isWhite ? snapshot.whiteScore() : snapshot.blackScore();
        java.util.List<MoveLogRow> log = isWhite ? snapshot.whiteMoveLog() : snapshot.blackMoveLog();

        int swatchSize = 14;
        int headerY = panelY + 10;
        canvas.fillRect(x, headerY, swatchSize, swatchSize, swatchColor);
        canvas.drawRect(x, headerY, swatchSize, swatchSize, Color.DARK_GRAY, 1);
        canvas.putText(colorLabel + "  " + playerName + " - " + score,
                x + swatchSize + 8, headerY + swatchSize, 1.1f, Color.BLACK, 1);

        int dividerY = panelY + 35;
        canvas.fillRect(x, dividerY, SIDEBAR_WIDTH - 20, 1, Color.GRAY);

        int headerRowY = panelY + 55;
        canvas.putText("Time", x, headerRowY, 0.8f, Color.DARK_GRAY, 1);
        canvas.putText("Move", x + 50, headerRowY, 0.8f, Color.DARK_GRAY, 1);

        int rowY = headerRowY + 20;
        for (MoveLogRow row : log) {
            if (rowY > panelY + panelHeight - 10) {
                break;
            }
            canvas.putText(row.time(), x, rowY, 0.75f, Color.BLUE, 1);
            canvas.putText(row.description(), x + 50, rowY, 0.75f, Color.BLUE, 1);
            rowY += 18;
        }
    }

    private void drawDisconnectCountdown(Img canvas, RenderSnapshot snapshot) {
        String text = "Opponent disconnected - auto-resign in " + snapshot.disconnectSecondsLeft() + "s";

        int boardPixelWidth = snapshot.boardWidth() * cellSize;
        int boardPixelHeight = snapshot.boardHeight() * cellSize;

        int overlayHeight = 90;
        int overlayY = boardPixelHeight / 2 - overlayHeight / 2;
        canvas.fillRect(0, overlayY, boardPixelWidth, overlayHeight, new Color(0, 0, 0, 160));

        float textScale = 1.6f;
        int approxTextWidth = (int) (text.length() * 7.5f * textScale);
        int textX = Math.max(10, (boardPixelWidth - approxTextWidth) / 2);
        int textY = overlayY + 35;
        canvas.putText(text, textX, textY, textScale, Color.ORANGE, 2);

        int barX = 40;
        int barY = overlayY + 55;
        int barWidth = boardPixelWidth - 80;
        int barHeight = 18;

        canvas.drawRect(barX, barY, barWidth, barHeight, Color.WHITE, 2);

        double fraction = 0.0;
        Integer total = snapshot.disconnectTotalSeconds();
        if (total != null && total > 0) {
            fraction = Math.max(0.0, Math.min(1.0, snapshot.disconnectSecondsLeft() / (double) total));
        }
        int filledWidth = (int) Math.round((barWidth - 4) * fraction);
        if (filledWidth > 0) {
            canvas.fillRect(barX + 2, barY + 2, filledWidth, barHeight - 4, new Color(255, 90, 0, 220));
        }
    }

    private void drawGameOver(Img canvas, RenderSnapshot snapshot) {
        String text = snapshot.winner() == null ? "GAME OVER" : snapshot.winner() + " WINS!";
        if (snapshot.returnCountdownSecondsLeft() != null) {
            text += " - Returning to matchmaking in " + snapshot.returnCountdownSecondsLeft() + "s";
        }

        int boardPixelWidth = snapshot.boardWidth() * cellSize;
        int boardPixelHeight = snapshot.boardHeight() * cellSize;

        canvas.fillRect(0, boardPixelHeight / 2 - 60, boardPixelWidth, 120,
                new java.awt.Color(0, 0, 0, 160));

        int approxTextWidth = text.length() * 34;
        int x = Math.max(10, (boardPixelWidth - approxTextWidth) / 2);
        int y = boardPixelHeight / 2 + 15;

        canvas.putText(text, x, y, 4.5f, Color.RED, 4);
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

    private void drawRestingIndicators(Img canvas, RenderSnapshot snapshot) {
        for (PieceRenderSnapshot piece : snapshot.pieces()) {
            if (!piece.stateFolder().equals("long_rest") && !piece.stateFolder().equals("short_rest")) {
                continue;
            }

            long totalDuration = piece.stateFolder().equals("long_rest")
                    ? engine.GameEngine.LONG_REST_DURATION
                    : engine.GameEngine.SHORT_REST_DURATION;

            double progress = Math.min(1.0, piece.stateElapsedMillis() / (double) totalDuration);
            int alpha = (int) Math.round(180 * (1.0 - progress));   // 180 בהתחלה -> 0 בסוף
            if (alpha <= 0) continue;

            int x = (int) Math.round(piece.pixelX());
            int y = (int) Math.round(piece.pixelY());

            canvas.fillRect(x, y, cellSize, cellSize, new Color(255, 215, 0, alpha));
        }
    }

    public void setOnRightClick(ClickListener listener) {
        gameWindow.setRightClickListener(listener);
    }
}