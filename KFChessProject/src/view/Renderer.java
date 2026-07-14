package view;

import io.BoardParser;
import models.Board;
import models.Piece;
import models.Position;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {

    private static final int CELL_SIZE = 100;

    //  מבנה נתונים שמחזיק לכל כלי — את ה-animator שלו
    private final Map<Piece, PieceAnimator> animators = new HashMap<>();




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

    //מתודת עזר  להמרת Piece לקידומת שם תיקייה
    private String pieceCode(Piece piece) {
        String color = piece.getColor().name().substring(0, 1); // "W" / "B"
        String type = switch (piece.getType()) {
            case KING -> "K"; case QUEEN -> "Q"; case ROOK -> "R";
            case BISHOP -> "B"; case KNIGHT -> "N"; case PAWN -> "P";
        };
        return type + color; // לדוגמה "QW"
    }

    //בניית לוח אמיתי עם חיבור לאויבקט לוח
    public void renderFromRealBoard(Board board, long currentClock)throws java.io.IOException {
        Img canvas = new Img().read("resources/board.png");

        int boardPixelWidth = canvas.get().getWidth();
        int boardPixelHeight = canvas.get().getHeight();

        int cellWidth = boardPixelWidth / board.getWidth();
        int cellHeight = boardPixelHeight / board.getHeight();

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece == null) continue;

                PieceAnimator animator = getAnimatorFor(piece);
                animator.update(currentClock);

                Img pieceImg = new Img().read(
                        animator.getCurrentSpritePath(),
                        new Dimension(cellWidth, cellHeight),
                        true, null
                );
                pieceImg.drawOn(canvas, col * cellWidth, row * cellHeight);
            }
        }
        canvas.show();
    }

    //מתודת עזר שמביאה (או יוצרת, אם עוד לא קיים) את ה-animator של כלי מסוים:
    private PieceAnimator getAnimatorFor(Piece piece) throws java.io.IOException {
        PieceAnimator animator = animators.get(piece);
        if (animator == null) {
            String folder = "resources/pieces1/" + pieceCode(piece);
            animator = new PieceAnimator(folder);
            animators.put(piece, animator);
        }
        return animator;
    }


}


