import engine.GameEngine;
import input.BoardMapper;
import input.Controller;
import models.Board;
import models.GameState;
import models.MatrixBoard;
import models.Piece;
import models.Position;
import models.enums.PieceColor;
import models.enums.PieceState;
import models.enums.PieceType;
import org.junit.jupiter.api.Test;
import realtime.RealTimeArbiter;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {

    private static final int CS = BoardMapper.CELL_SIZE;

    private GameEngine newEngine(Board board) {
        return new GameEngine(board, new RealTimeArbiter(), new GameState());
    }

    @Test
    void selectingPieceThenClickingLegalDestinationTriggersMove() {
        Board board = new MatrixBoard(8, 8);
        Position rookPos = new Position(1, 1);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, rookPos);
        board.addPiece(rookPos, rook);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        controller.handleMouseClick(1 * CS + 1, 1 * CS + 1, 0L); // בוחר את הצריח ב-(1,1)
        controller.handleMouseClick(4 * CS + 1, 1 * CS + 1, 0L); // מנסה מהלך חוקי ל-(1,4)

        assertEquals(PieceState.AIRBORNE, rook.getState(), "לאחר בחירה ולחיצה על יעד חוקי, המהלך אמור להיות מופעל");
    }

    @Test
    void clickingEmptyCellFirstDoesNotSelectAnything() {
        Board board = new MatrixBoard(8, 8);
        Position rookPos = new Position(1, 1);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, rookPos);
        board.addPiece(rookPos, rook);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        controller.handleMouseClick(0 * CS + 1, 0 * CS + 1, 0L); // (0,0) ריק - לא אמור לבחור כלום

        // הלחיצה השנייה על תא ריק נוסף אמורה גם היא שלא לקרוס ולא לגרום למהלך
        controller.handleMouseClick(3 * CS + 1, 3 * CS + 1, 0L);

        assertEquals(PieceState.IDLE, rook.getState(), "אם לא נבחר כלי, לא אמור להתבצע שום מהלך");
        assertEquals(rook, board.getPieceAt(rookPos), "הכלי חייב להישאר במקומו המקורי");
    }

    @Test
    void clickingOwnColorPieceSwitchesSelectionWithoutMoving() {
        Board board = new MatrixBoard(8, 8);
        Position pos1 = new Position(1, 1);
        Position pos2 = new Position(1, 4);
        Piece rook1 = new Piece("r1", PieceColor.WHITE, PieceType.ROOK, pos1);
        Piece rook2 = new Piece("r2", PieceColor.WHITE, PieceType.ROOK, pos2);
        board.addPiece(pos1, rook1);
        board.addPiece(pos2, rook2);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        controller.handleMouseClick(1 * CS + 1, 1 * CS + 1, 0L); // בוחר rook1
        controller.handleMouseClick(4 * CS + 1, 1 * CS + 1, 0L); // לוחץ על rook2 (אותו צבע) - אמור להחליף בחירה בלבד

        assertEquals(PieceState.IDLE, rook1.getState(), "כלי שהוחלפה הבחירה ממנו אינו אמור לזוז");
        assertEquals(PieceState.IDLE, rook2.getState(), "מעבר בחירה בין כלים מאותו צבע אינו אמור להפעיל מהלך");

        controller.handleMouseClick(6 * CS + 1, 1 * CS + 1, 0L); // עכשיו מנסה מהלך חוקי עבור rook2 בלבד

        assertEquals(PieceState.AIRBORNE, rook2.getState(), "לאחר החלפת הבחירה, rook2 חייב להיות זה שמבצע את המהלך");
        assertEquals(PieceState.IDLE, rook1.getState(), "rook1 אינו אמור להיות מושפע מהמהלך של rook2");
    }

    @Test
    void clickingEnemyPieceAttemptsCapture() {
        Board board = new MatrixBoard(8, 8);
        Position rookPos = new Position(1, 1);
        Position enemyPos = new Position(1, 4);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, rookPos);
        Piece enemyPawn = new Piece("p", PieceColor.BLACK, PieceType.PAWN, enemyPos);
        board.addPiece(rookPos, rook);
        board.addPiece(enemyPos, enemyPawn);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        controller.handleMouseClick(1 * CS + 1, 1 * CS + 1, 0L); // בוחר את הצריח
        controller.handleMouseClick(4 * CS + 1, 1 * CS + 1, 0L); // לוחץ על כלי יריב - ניסיון לכידה

        assertEquals(PieceState.AIRBORNE, rook.getState(), "לחיצה על כלי יריב במהלך חוקי חייבת להפעיל ניסיון לכידה");
    }

    @Test
    void clickingOutsideBoardBoundsDoesNothing() {
        Board board = new MatrixBoard(4, 4);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        assertDoesNotThrow(() -> controller.handleMouseClick(10000, 10000, 0L),
                "לחיצה מחוץ לגבולות הלוח אסורה שתגרום לשגיאה");
    }

    @Test
    void jumpCommandTriggersJumpingStateOnPiece() {
        Board board = new MatrixBoard(8, 8);
        Position pos = new Position(2, 2);
        Piece pawn = new Piece("p", PieceColor.WHITE, PieceType.PAWN, pos);
        board.addPiece(pos, pawn);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        controller.handleJumpCommand(2 * CS + 1, 2 * CS + 1, 1000L);

        assertEquals(PieceState.JUMPING, pawn.getState(), "פקודת קפיצה על כלי קיים חייבת להעביר אותו למצב JUMPING");
    }

    @Test
    void jumpCommandOnEmptyCellDoesNothing() {
        Board board = new MatrixBoard(8, 8);
        GameEngine engine = newEngine(board);
        Controller controller = new Controller(engine, board);

        assertDoesNotThrow(() -> controller.handleJumpCommand(2 * CS + 1, 2 * CS + 1, 1000L),
                "פקודת קפיצה על תא ריק אסור שתגרום לשגיאה");
    }
}
