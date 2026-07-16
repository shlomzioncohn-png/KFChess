package tests;

import engine.GameEngine;
import io.BoardParser;
import input.Controller;
import models.Board;
import models.GameState;
import models.Position;
import models.enums.PieceState;
import org.junit.jupiter.api.Test;
import realtime.RealTimeArbiter;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {

    private static final int CELL_SIZE = 100;

    private int pixelX(int col) { return col * CELL_SIZE + 50; }
    private int pixelY(int row) { return row * CELL_SIZE + 50; }

    private Controller buildController(Board board) {
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, arbiter, gameState);
        return new Controller(engine, board);
    }

    @Test
    public void clickingOwnPieceSelectsIt() {
        Board board = BoardParser.parse("wP . . .\n.  .  .  .\n.  .  .  .\n.  .  .  .");
        Controller controller = buildController(board);

        controller.handleMouseClick(pixelX(0), pixelY(0));

        assertEquals(new Position(0, 0), controller.getSelectedPosition());
    }

    @Test
    public void clickingEmptySquareFirstDoesNothing() {
        Board board = BoardParser.parse("wP . . .\n.  .  .  .\n.  .  .  .\n.  .  .  .");
        Controller controller = buildController(board);

        controller.handleMouseClick(pixelX(2), pixelY(2));

        assertNull(controller.getSelectedPosition());
    }

    @Test
    public void secondClickOnDifferentColorAttemptsMoveAndClearsSelection() {
        Board board = BoardParser.parse(".  .  .  .\n.  wP .  .\n.  .  .  .\n.  .  .  .");
        Controller controller = buildController(board);

        controller.handleMouseClick(pixelX(1), pixelY(1)); // select wP at (1,1)
        assertEquals(new Position(1, 1), controller.getSelectedPosition());

        controller.handleMouseClick(pixelX(1), pixelY(0)); // attempt move to (0,1)
        assertNull(controller.getSelectedPosition());
    }

    @Test
    public void secondClickOnSameColorPieceReselectsInsteadOfMoving() {
        Board board = BoardParser.parse("wP .  wP .\n.  .  .  .\n.  .  .  .\n.  .  .  .");
        Controller controller = buildController(board);

        controller.handleMouseClick(pixelX(0), pixelY(0)); // select first wP
        controller.handleMouseClick(pixelX(2), pixelY(0)); // click second wP (same color)

        assertEquals(new Position(0, 2), controller.getSelectedPosition());
    }

    @Test
    public void jumpCommandSetsPieceToJumpingState() {
        Board board = BoardParser.parse("wP . . .\n.  .  .  .\n.  .  .  .\n.  .  .  .");
        Controller controller = buildController(board);

        controller.handleJumpCommand(pixelX(0), pixelY(0));

        assertEquals(PieceState.JUMPING, board.getPieceAt(new Position(0, 0)).getState());
    }

    @Test
    public void updateAdvancesEngineClockAndResolvesCompletedMotions() {
        Board board = BoardParser.parse(".  .  .  .\n.  wP .  .\n.  .  .  .\n.  .  .  .");
        Controller controller = buildController(board);

        controller.handleMouseClick(pixelX(1), pixelY(1)); // select
        controller.handleMouseClick(pixelX(1), pixelY(0)); // move attempt to (0,1)

        // מרחק משבצת אחת = 1000ms טיסה; מקדמים מעבר לזה
        controller.update(1200);

        assertNull(board.getPieceAt(new Position(1, 1)));
        assertNotNull(board.getPieceAt(new Position(0, 1)));
    }
}