package view;

import java.awt.Dimension;

public class Renderer {

    private static final int CELL_SIZE = 100;


    //  יוצרת img חדש וטוענת לתוכו את board.png ומציגה אותו
    public void renderStaticBoard() {
        Img canvas = new Img().read("resources/board.png");
        canvas.show();
    }

    //תמונה על גבי תמונה- בדיקה שזה עובד
    public void renderSinglePieceDemo() {
        Img canvas = new Img().read("resources/board.png");

        int boardPixelWidth = 822;
        int boardPixelHeight = 828;
        int cellWidth = boardPixelWidth / 8;
        int cellHeight = boardPixelHeight / 8;

        Img piece = new Img().read(
                "resources/pieces1/QW/states/idle/sprites/1.png",
                new Dimension(cellWidth, cellHeight),
                true,
                null
        );

        int row = 7;
        int col = 4;
        int x = col * cellWidth;
        int y = row * cellHeight;

        piece.drawOn(canvas, x, y);
        canvas.show();
    }
    public static void main(String[] args) {
        new Renderer().renderSinglePieceDemo();
    }
}


