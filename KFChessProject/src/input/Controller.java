package input;

import engine.GameEngine;
import models.Board;
import models.Position;

public class Controller {
    private final GameEngine engine;
    private final Board board;

    private Position selectedPosition = null;

    public Controller(GameEngine engine, Board board) {
        this.engine = engine;
        this.board = board;
    }

    /**
     * פונקציה מרכזית לטיפול בכל לחיצה על הלוח.
     */
    public void handleMouseClick(int x, int y, long currentClock) {
        Position pos = BoardMapper.mapPixelToPosition(x, y, board);
        if (pos == null) return; // לחיצה מחוץ ללוח

        if (selectedPosition == null) {
            if (board.getPieceAt(pos) != null) {
                selectedPosition = pos;
                System.out.println("Selected piece at: " + pos);
            }
        }
        else {
            if (board.getPieceAt(pos) != null && board.getPieceAt(pos).getColor() == board.getPieceAt(selectedPosition).getColor()) {
                selectedPosition = pos;
            } else {
                System.out.println("Attempting move from " + selectedPosition + " to " + pos);

                engine.tryMove(selectedPosition, pos, currentClock);
                selectedPosition = null; // מאפסים לאחר הניסיון
            }
        }
    }

    /**
     * לוגיקת הקפיצה (Jump) - מוכנה להטמעה!
     */
    public void handleJumpCommand(int x, int y, long currentClock) {
        Position pos = BoardMapper.mapPixelToPosition(x, y, board);
        if (pos != null && board.getPieceAt(pos) != null) {
            engine.triggerJump(pos, currentClock);   // <-- זה השינוי - היה מוער בהערה
        }
    }

    public void update(long currentClock) {
        engine.update(currentClock);
    }

    public Position getSelectedPosition() {
        return selectedPosition;
    }
}
