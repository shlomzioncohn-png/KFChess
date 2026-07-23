package  tests;

import bus.EventBus;
import bus.EventListener;
import bus.events.IllegalMoveEvent;
import client.GameClient;
import engine.GameEngine;
import models.Board;
import models.GameState;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import input.Controller;
import realtime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ControllerTest {

    private static final int CELL_SIZE = 100;

    private static int pixelX(int col) { return col * CELL_SIZE + 50; }
    private static int pixelY(int row) { return row * CELL_SIZE + 50; }

    private record Fixture(Board board, GameEngine engine, GameClient client, Controller controller) {}

    private Fixture newFixture(Board board) {
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, arbiter, gameState, new EventBus());
        GameClient client = mock(GameClient.class);
        Controller controller = new Controller(engine, board, client, () -> CELL_SIZE);
        return new Fixture(board, engine, client, controller);
    }

    @Test
    void clickingOwnPieceSelectsIt() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 0), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0)));
        Fixture fx = newFixture(board);

        fx.controller().handleMouseClick(pixelX(0), pixelY(0));

        assertEquals(new Position(0, 0), fx.controller().getSelectedPosition(),
                "לחיצה על כלי של השחקן חייבת לבחור אותו");
    }

    @Test
    void clickingEmptySquareFirstSelectsNothing() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 0), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0)));
        Fixture fx = newFixture(board);

        fx.controller().handleMouseClick(pixelX(2), pixelY(2));

        assertNull(fx.controller().getSelectedPosition(), "לחיצה ראשונה על משבצת ריקה אסורה לבחור כלום");
    }

    @Test
    void clickOutsideBoardBoundsLeavesSelectionUnchanged() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 0), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0)));
        Fixture fx = newFixture(board);

        fx.controller().handleMouseClick(pixelX(0), pixelY(0));
        assertEquals(new Position(0, 0), fx.controller().getSelectedPosition());

        fx.controller().handleMouseClick(1000, 1000); // מחוץ ללוח 8x8 בגודל תא 100

        assertEquals(new Position(0, 0), fx.controller().getSelectedPosition(),
                "לחיצה מחוץ לגבולות הלוח אסורה לשנות בחירה קיימת");
        verify(fx.client(), never()).send(anyString());
    }

    @Test
    void secondClickOnSameColorPieceReselectsInsteadOfMoving() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 0), new Piece("wp1", PieceColor.WHITE, PieceType.PAWN, new Position(0, 0)));
        board.addPiece(new Position(0, 2), new Piece("wp2", PieceColor.WHITE, PieceType.PAWN, new Position(0, 2)));
        Fixture fx = newFixture(board);

        fx.controller().handleMouseClick(pixelX(0), pixelY(0));
        fx.controller().handleMouseClick(pixelX(2), pixelY(0));

        assertEquals(new Position(0, 2), fx.controller().getSelectedPosition(),
                "לחיצה שנייה על כלי מאותו צבע חייבת להחליף את הבחירה, לא לנסות מהלך");
        verify(fx.client(), never()).send(anyString());
    }

    @Test
    void secondClickOnIllegalDestinationPublishesIllegalEventAndClearsSelection() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(4, 4), new Piece("r1", PieceColor.WHITE, PieceType.ROOK, new Position(4, 4)));
        Fixture fx = newFixture(board);

        List<IllegalMoveEvent> captured = new ArrayList<>();
        fx.engine().getBus().subscribe("move.illegal",
                (topic, payload) -> captured.add((IllegalMoveEvent) payload));

        fx.controller().handleMouseClick(pixelX(4), pixelY(4));
        fx.controller().handleMouseClick(pixelX(5), pixelY(5));

        assertEquals(1, captured.size(), "מהלך לא חוקי חייב לפרסם בדיוק אירוע move.illegal אחד");
        assertEquals(new Position(4, 4), captured.get(0).getFrom());
        assertEquals(new Position(5, 5), captured.get(0).getTo());
        assertNull(fx.controller().getSelectedPosition(), "אחרי מהלך לא חוקי הבחירה חייבת להתאפס");
        verify(fx.client(), never()).send(anyString());
    }

    @Test
    void secondClickOnLegalDestinationSendsExactMoveCommandAndClearsSelection() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(4, 4), new Piece("r1", PieceColor.WHITE, PieceType.ROOK, new Position(4, 4)));
        Fixture fx = newFixture(board);

        fx.controller().handleMouseClick(pixelX(4), pixelY(4));
        fx.controller().handleMouseClick(pixelX(7), pixelY(4));

        // (4,4) -> קובץ e, מסלול 8-4=4 => e4 ; (4,7) -> קובץ h, מסלול 4 => h4
        verify(fx.client(), times(1)).send("WRe4h4");
        assertNull(fx.controller().getSelectedPosition(), "אחרי שליחת מהלך חוקי הבחירה חייבת להתאפס");
    }

    @Test
    void clientSendExceptionDuringMoveDoesNotPropagate() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(4, 4), new Piece("r1", PieceColor.WHITE, PieceType.ROOK, new Position(4, 4)));
        Fixture fx = newFixture(board);
        doThrow(new RuntimeException("not connected")).when(fx.client()).send(anyString());

        fx.controller().handleMouseClick(pixelX(4), pixelY(4));
        assertDoesNotThrow(() -> fx.controller().handleMouseClick(pixelX(7), pixelY(4)),
                "כשל בשליחה לשרת אסור שיזרוק חריגה החוצה מ-Controller");
        assertNull(fx.controller().getSelectedPosition(), "הבחירה חייבת להתאפס גם כששליחת הפקודה נכשלה");
    }

    @Test
    void staleSelectionOfPieceThatMovedAwayIsClearedAndTreatedAsFreshClick() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(1, 1), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(1, 1)));
        board.addPiece(new Position(3, 3), new Piece("bp", PieceColor.BLACK, PieceType.PAWN, new Position(3, 3)));
        Fixture fx = newFixture(board);

        fx.controller().handleMouseClick(pixelX(1), pixelY(1));
        assertEquals(new Position(1, 1), fx.controller().getSelectedPosition());

        board.removePiece(new Position(1, 1)); // בזמן-אמת הכלי הזה כבר לא שם (זז/נאכל)

        fx.controller().handleMouseClick(pixelX(3), pixelY(3));

        assertEquals(new Position(3, 3), fx.controller().getSelectedPosition(),
                "בחירה של כלי שכבר לא במקומו חייבת להתבטל, והקליק החדש חייב להיחשב כקליק ראשון טרי");
        verify(fx.client(), never()).send(anyString());
    }

    @Test
    void handleJumpCommandOnOwnPieceSendsExactJumpCommand() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(2, 3), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(2, 3)));
        Fixture fx = newFixture(board);

        fx.controller().handleJumpCommand(pixelX(3), pixelY(2));

        // (2,3) -> קובץ d, מסלול 8-2=6 => d6
        verify(fx.client(), times(1)).send("WJd6");
    }

    @Test
    void handleJumpCommandOnEmptyCellDoesNothing() {
        Board board = new MatrixBoard(8, 8);
        Fixture fx = newFixture(board);

        fx.controller().handleJumpCommand(pixelX(3), pixelY(3));

        verify(fx.client(), never()).send(anyString());
    }

    @Test
    void handleJumpCommandOutsideBoardDoesNothing() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(2, 3), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(2, 3)));
        Fixture fx = newFixture(board);

        fx.controller().handleJumpCommand(5000, 5000);

        verify(fx.client(), never()).send(anyString());
    }

    @Test
    void updateDelegatesElapsedTimeToEngineClockCumulatively() {
        Board board = new MatrixBoard(8, 8);
        Fixture fx = newFixture(board);

        fx.controller().update(300);
        fx.controller().update(200);

        assertEquals(500L, fx.engine().getGameClockMs(),
                "update() חייב להעביר את הדלתא במדויק ל-engine.waitMs ולצבור אותה בשעון");
    }
}
