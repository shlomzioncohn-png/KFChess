package view;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

    //מתודת עזר שמגיעה לניתוב הנכון בתקיה לפי ה-"QW", "NB וכו'...
    private String pieceFolder(String code) {
        return "resources/pieces1/" + code + "/states/idle/sprites/1.png";
    }

    //שם את הכלים על הלוח
    public void renderFullBoardFromCsv() throws IOException {
        Img canvas = new Img().read("resources/board.png");

        int boardPixelWidth = 822;
        int boardPixelHeight = 828;
        int cellWidth = boardPixelWidth / 8;
        int cellHeight = boardPixelHeight / 8;

        List<String> lines = Files.readAllLines(Paths.get("resources/pieces1/board.csv"));

        for (int row = 0; row < lines.size(); row++) {
            String[] cells = lines.get(row).split(",");
            for (int col = 0; col < cells.length; col++) {
                String code = cells[col].trim();
                if (code.isEmpty()) continue;

                Img piece = new Img().read(
                        pieceFolder(code),
                        new Dimension(cellWidth, cellHeight),
                        true,
                        null
                );

                int x = col * cellWidth;
                int y = row * cellHeight;
                piece.drawOn(canvas, x, y);
            }
        }

        canvas.show();
    }

    public static void main(String[] args) {
        try {
            new Renderer().renderFullBoardFromCsv();
        }
        catch (IOException ex) {}
    }
}


