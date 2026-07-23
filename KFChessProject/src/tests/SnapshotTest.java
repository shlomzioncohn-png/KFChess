package  tests;

import bus.EventBus;
import engine.GameEngine;
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
import view.MoveLogRow;
import view.PieceRenderSnapshot;
import view.RenderSnapshot;
import view.SnapshotFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SnapshotTest {

    private static final int CELL_SIZE = 100;

    private RenderSnapshot build(Board board, GameEngine engine, RealTimeArbiter arbiter,
                                  Position selectedPosition, GameState gameState) {
        return SnapshotFactory.build(board, engine, arbiter, selectedPosition, CELL_SIZE, gameState,
                "White", "Black", null, null, null, null, "ROOM1");
    }

    @Test
    void snapshotContainsCorrectPieceCountAndExcludesCaptured() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 0), new Piece("wr", PieceColor.WHITE, PieceType.ROOK, new Position(0, 0)));
        board.addPiece(new Position(7, 7), new Piece("bk", PieceColor.BLACK, PieceType.KING, new Position(7, 7)));
        Piece captured = new Piece("dead", PieceColor.WHITE, PieceType.PAWN, new Position(3, 3));
        captured.setState(PieceState.CAPTURED);
        board.addPiece(new Position(3, 3), captured);

        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot snapshot = build(board, engine, new RealTimeArbiter(), null, gameState);

        assertEquals(2, snapshot.pieces().size(), "כלי CAPTURED אסור שיופיע בסנאפשוט");
        List<String> ids = snapshot.pieces().stream().map(PieceRenderSnapshot::id).toList();
        assertTrue(ids.contains("wr") && ids.contains("bk"));
        assertFalse(ids.contains("dead"));
    }

    @Test
    void renderingDoesNotMutateGameState() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(0, 0), new Piece("wr", PieceColor.WHITE, PieceType.ROOK, new Position(0, 0)));
        GameState gameState = new GameState();
        gameState.addScore(PieceColor.WHITE, 3);
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        build(board, engine, new RealTimeArbiter(), null, gameState);
        int scoreAfterFirst = gameState.getScore(PieceColor.WHITE);
        build(board, engine, new RealTimeArbiter(), null, gameState);

        assertEquals(3, scoreAfterFirst, "בניית סנאפשוט אסורה לשנות את הניקוד");
        assertEquals(3, gameState.getScore(PieceColor.WHITE), "בניית סנאפשוט חוזרת אסורה לשנות את מצב המשחק");
    }

    @Test
    void selectedPositionIsPassedThroughOrNullWhenNoneGiven() {
        Board board = new MatrixBoard(8, 8);
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());
        Position selected = new Position(2, 2);

        RenderSnapshot withSelection = build(board, engine, new RealTimeArbiter(), selected, gameState);
        RenderSnapshot withoutSelection = build(board, engine, new RealTimeArbiter(), null, gameState);

        assertEquals(selected, withSelection.selectedPosition());
        assertNull(withoutSelection.selectedPosition());
    }

    @Test
    void legalMovesPopulatedOnlyWhenPieceSelected() {
        Board board = new MatrixBoard(8, 8);
        Position rookPos = new Position(4, 4);
        board.addPiece(rookPos, new Piece("wr", PieceColor.WHITE, PieceType.ROOK, rookPos));
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot selected = build(board, engine, new RealTimeArbiter(), rookPos, gameState);
        RenderSnapshot notSelected = build(board, engine, new RealTimeArbiter(), null, gameState);

        assertEquals(14, selected.legalMoves().size(),
                "צריח בודד באמצע לוח 8x8 חייב לקבל בדיוק 14 יעדים חוקיים (7 בשורה + 7 בעמודה)");
        assertTrue(selected.legalMoves().contains(new Position(4, 0)));
        assertTrue(selected.legalMoves().contains(new Position(4, 7)));
        assertTrue(selected.legalMoves().contains(new Position(0, 4)));
        assertTrue(selected.legalMoves().contains(new Position(7, 4)));
        assertFalse(selected.legalMoves().contains(new Position(5, 5)), "אלכסון אסור להופיע ביעדים החוקיים של צריח");
        assertFalse(selected.legalMoves().contains(rookPos));
        assertTrue(notSelected.legalMoves().isEmpty(), "ללא בחירה, רשימת היעדים החוקיים חייבת להיות ריקה");
    }

    @Test
    void gameOverAndWinnerOnlyReflectedWhenGameIsActuallyOver() {
        Board board = new MatrixBoard(8, 8);
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        gameState.setWinner(PieceColor.WHITE); // מוגדר אך המשחק עדיין לא הסתיים רשמית
        RenderSnapshot notOverYet = build(board, engine, new RealTimeArbiter(), null, gameState);
        assertFalse(notOverYet.gameOver());
        assertNull(notOverYet.winner(), "כל עוד gameOver=false, המנצח אסור להופיע בסנאפשוט");

        gameState.setGameOver(true);
        RenderSnapshot over = build(board, engine, new RealTimeArbiter(), null, gameState);
        assertTrue(over.gameOver());
        assertEquals("WHITE", over.winner());
    }

    @Test
    void scoresAreReflectedFromGameState() {
        Board board = new MatrixBoard(8, 8);
        GameState gameState = new GameState();
        gameState.addScore(PieceColor.WHITE, 5);
        gameState.addScore(PieceColor.BLACK, 2);
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot snapshot = build(board, engine, new RealTimeArbiter(), null, gameState);

        assertEquals(5, snapshot.whiteScore());
        assertEquals(2, snapshot.blackScore());
    }

    @Test
    void moveLogIsTruncatedToLastTenEntriesPerPlayerInOrder() {
        Board board = new MatrixBoard(8, 8);
        GameState gameState = new GameState();
        for (int i = 0; i < 12; i++) {
            gameState.addLogEntry(PieceColor.WHITE, "move#" + i, i * 1000L);
        }
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot snapshot = build(board, engine, new RealTimeArbiter(), null, gameState);

        List<MoveLogRow> whiteLog = snapshot.whiteMoveLog();
        assertEquals(10, whiteLog.size(), "רק 10 השורות האחרונות אמורות להישמר");
        assertEquals("move#2", whiteLog.get(0).description(), "השורה הראשונה שנשארה חייבת להיות ה-3 (אינדקס 2), לא הראשונה");
        assertEquals("move#11", whiteLog.get(9).description(), "השורה האחרונה חייבת להיות התנועה האחרונה שנרשמה");
        assertTrue(snapshot.blackMoveLog().isEmpty());
    }

    @Test
    void moveLogClockIsFormattedAsMinutesColonSeconds() {
        Board board = new MatrixBoard(8, 8);
        GameState gameState = new GameState();
        gameState.addLogEntry(PieceColor.BLACK, "e4e5", 65_000L); // דקה אחת וחמש שניות
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot snapshot = build(board, engine, new RealTimeArbiter(), null, gameState);

        assertEquals("1:05", snapshot.blackMoveLog().get(0).time());
    }

    @Test
    void idlePiecePixelPositionEqualsCellTimesCellSize() {
        Board board = new MatrixBoard(8, 8);
        board.addPiece(new Position(2, 3), new Piece("wp", PieceColor.WHITE, PieceType.PAWN, new Position(2, 3)));
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot snapshot = build(board, engine, new RealTimeArbiter(), null, gameState);
        PieceRenderSnapshot p = snapshot.pieces().get(0);

        assertEquals("idle", p.stateFolder());
        assertEquals(300.0, p.pixelX(), 0.001);
        assertEquals(200.0, p.pixelY(), 0.001);
    }

    @Test
    void airbornePieceInterpolatesPositionAccordingToProgress() {
        Board board = new MatrixBoard(8, 8);
        Position src = new Position(4, 4);
        Position dest = new Position(4, 6); // מרחק 2 -> משך טיסה 2000ms
        board.addPiece(src, new Piece("wr", PieceColor.WHITE, PieceType.ROOK, src));
        GameState gameState = new GameState();
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameEngine engine = new GameEngine(board, arbiter, gameState, new EventBus());

        engine.tryMove(src, dest);
        engine.waitMs(500L); // 25% מתוך 2000ms

        RenderSnapshot snapshot = build(board, engine, arbiter, null, gameState);
        PieceRenderSnapshot p = snapshot.pieces().get(0);

        assertEquals("move", p.stateFolder());
        double expectedX = 400.0 + (600.0 - 400.0) * 0.25; // startX + (endX-startX)*progress
        assertEquals(expectedX, p.pixelX(), 0.001);
        assertEquals(400.0, p.pixelY(), 0.001, "מהלך אופקי - ה-Y לא אמור להשתנות");
    }

    @Test
    void jumpingPieceGetsUpwardArcOffsetAndJumpStateFolder() {
        Board board = new MatrixBoard(8, 8);
        Position pos = new Position(2, 2);
        board.addPiece(pos, new Piece("wp", PieceColor.WHITE, PieceType.PAWN, pos));
        GameState gameState = new GameState();
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameEngine engine = new GameEngine(board, arbiter, gameState, new EventBus());

        engine.triggerJump(pos);
        engine.waitMs(500L); // באמצע קפיצה של 1000ms

        RenderSnapshot snapshot = build(board, engine, arbiter, null, gameState);
        PieceRenderSnapshot p = snapshot.pieces().get(0);

        assertEquals("jump", p.stateFolder());
        assertEquals(200.0, p.pixelX(), 0.001, "קפיצה במקום אסורה לשנות X");
        assertTrue(p.pixelY() < 200.0, "באמצע קפיצה הכלי חייב להיות מוזז כלפי מעלה (Y קטן יותר) מהמיקום הבסיסי");
    }

    @Test
    void longRestingPieceReportsCorrectStateFolderWithNonNegativeElapsed() {
        Board board = new MatrixBoard(8, 8);
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        board.addPiece(src, new Piece("wr", PieceColor.WHITE, PieceType.ROOK, src));
        GameState gameState = new GameState();
        RealTimeArbiter arbiter = new RealTimeArbiter();
        GameEngine engine = new GameEngine(board, arbiter, gameState, new EventBus());

        engine.tryMove(src, dest);
        engine.waitMs(1000L); // מגיע בדיוק ליעד -> נכנס ל-LONG_RESTING עם elapsed=0

        RenderSnapshot snapshot = build(board, engine, arbiter, null, gameState);
        PieceRenderSnapshot p = snapshot.pieces().get(0);

        assertEquals("long_rest", p.stateFolder());
        assertTrue(p.stateElapsedMillis() >= 0, "זמן שחלף מאז תחילת המנוחה אסור להיות שלילי");
    }

    @Test
    void disconnectAndRoomInfoFieldsPassThroughExactlyIncludingNulls() {
        Board board = new MatrixBoard(8, 8);
        GameState gameState = new GameState();
        GameEngine engine = new GameEngine(board, new RealTimeArbiter(), gameState, new EventBus());

        RenderSnapshot snapshot = SnapshotFactory.build(board, engine, new RealTimeArbiter(), null, CELL_SIZE,
                gameState, "Alice", "Bob", 15, 20, null, null, "ABC123");

        assertEquals("Alice", snapshot.whitePlayerName());
        assertEquals("Bob", snapshot.blackPlayerName());
        assertEquals(15, snapshot.disconnectSecondsLeft());
        assertEquals(20, snapshot.disconnectTotalSeconds());
        assertNull(snapshot.returnCountdownSecondsLeft());
        assertNull(snapshot.returnCountdownTotalSeconds());
        assertEquals("ABC123", snapshot.roomId());
    }
}
