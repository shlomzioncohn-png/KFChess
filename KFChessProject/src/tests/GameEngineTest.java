package tests;

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

import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {

    private GameEngine newEngine(Board board, GameState gameState) {
        return new GameEngine(board, new RealTimeArbiter(), gameState);
    }

    @Test
    void tryMoveDoesNothingWhenGameIsOver() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        state.setGameOver(true);
        Position p1 = new Position(4, 4);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, p1);
        board.addPiece(p1, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(p1, new Position(4, 5));

        assertEquals(PieceState.IDLE, rook.getState(), "כאשר המשחק הסתיים, לא אמור להתבצע כל שינוי במצב הכלי");
        assertEquals(rook, board.getPieceAt(p1), "הכלי חייב להישאר במקומו כאשר המשחק הסתיים");
    }

    @Test
    void tryMoveDoesNothingWhenNoPieceAtSource() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        GameEngine engine = newEngine(board, state);

        assertDoesNotThrow(() -> engine.tryMove(new Position(0, 0), new Position(1, 1)),
                "ניסיון להזיז מתא ריק אינו אמור לזרוק שגיאה");
    }

    @Test
    void tryMoveIgnoredWhenPieceAlreadyAirborne() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position p1 = new Position(4, 4);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, p1);
        rook.setState(PieceState.AIRBORNE);
        board.addPiece(p1, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(p1, new Position(4, 5));
        engine.waitMs(5000L);

        assertEquals(rook, board.getPieceAt(p1), "כלי שכבר באוויר אינו אמור להתחיל מהלך חדש");
        assertEquals(PieceState.AIRBORNE, rook.getState(), "מצב הכלי חייב להישאר AIRBORNE");
    }

    @Test
    void tryMoveWithLegalMoveSetsPieceAirborneImmediately() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, new Position(4, 5));

        assertEquals(PieceState.AIRBORNE, rook.getState(), "מהלך חוקי חייב להעביר את הכלי מיידית למצב AIRBORNE");
        assertEquals(rook, board.getPieceAt(src), "לפני שהזמן חלף, הכלי עדיין אמור להופיע במשבצת המקורית");
    }

    @Test
    void tryMoveWithIllegalMoveDoesNotChangeState() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, new Position(6, 6)); // אלכסון - לא חוקי לצריח
        engine.waitMs(10000L);

        assertEquals(PieceState.IDLE, rook.getState(), "מהלך לא חוקי אסור שישנה את מצב הכלי");
        assertEquals(rook, board.getPieceAt(src), "מהלך לא חוקי אסור שיזיז את הכלי");
    }

    @Test
    void updateBeforeArrivalTimeDoesNotMovePieceYet() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, dest); // מרחק 1 -> זמן נסיעה 1000

        engine.waitMs(500L);

        assertEquals(rook, board.getPieceAt(src), "לפני שהגיע זמן היעד, הכלי לא אמור לזוז עדיין");
        assertNull(board.getPieceAt(dest), "לפני שהגיע זמן היעד, משבצת היעד אמורה להישאר ריקה");
        assertEquals(PieceState.AIRBORNE, rook.getState(), "הכלי אמור להישאר AIRBORNE בזמן הטיסה");
    }

    @Test
    void updateAfterArrivalTimeMovesPieceAndSetsIdle() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, dest);
        engine.waitMs(1000L);

        assertNull(board.getPieceAt(src), "אחרי שהגיע זמן היעד, המשבצת המקורית חייבת להתרוקן");
        assertEquals(rook, board.getPieceAt(dest), "אחרי שהגיע זמן היעד, הכלי חייב להופיע במשבצת היעד");
        assertEquals(PieceState.IDLE, rook.getState(), "אחרי סיום התנועה, מצב הכלי חייב לחזור ל-IDLE");
    }

    @Test
    void updateResolvesCaptureOfEnemyPiece() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        Piece enemyPawn = new Piece("p", PieceColor.BLACK, PieceType.PAWN, dest);
        board.addPiece(src, rook);
        board.addPiece(dest, enemyPawn);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, dest);
        engine.waitMs(1000L);

        assertEquals(rook, board.getPieceAt(dest), "אחרי לכידה, התוקף חייב להופיע במשבצת היעד");
        assertFalse(state.isGameOver(), "לכידת כלי שאינו מלך אינה אמורה לסיים את המשחק");
    }

    @Test
    void capturingKingEndsTheGameWithCorrectWinner() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        Piece enemyKing = new Piece("k", PieceColor.BLACK, PieceType.KING, dest);
        board.addPiece(src, rook);
        board.addPiece(dest, enemyKing);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, dest);
        engine.waitMs(1000L);

        assertTrue(state.isGameOver(), "לכידת מלך חייבת לסיים את המשחק");
        assertEquals(PieceColor.WHITE, state.getWinner(), "המנצח חייב להיות הצבע שביצע את הלכידה");
    }

    @Test
    void triggerJumpSetsJumpingStateAndExpiry() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position pos = new Position(2, 2);
        Piece pawn = new Piece("p", PieceColor.WHITE, PieceType.PAWN, pos);
        board.addPiece(pos, pawn);
        GameEngine engine = newEngine(board, state);

        engine.waitMs(1000L);   // מקדמים את שעון-המנוע ל-1000 לפני הפעלת הקפיצה
        engine.triggerJump(pos);

        assertEquals(PieceState.JUMPING, pawn.getState(), "לאחר triggerJump, מצב הכלי חייב להיות JUMPING");
        assertTrue(pawn.isProtectedByJump(1999L), "הכלי חייב להיות מוגן לפני תום 1000 המילישניות");
        assertFalse(pawn.isProtectedByJump(2000L), "הכלי אינו אמור להיות מוגן אחרי תום זמן ההגנה");
    }

    @Test
    void triggerJumpDoesNothingWhenGameIsOver() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        state.setGameOver(true);
        Position pos = new Position(2, 2);
        Piece pawn = new Piece("p", PieceColor.WHITE, PieceType.PAWN, pos);
        board.addPiece(pos, pawn);
        GameEngine engine = newEngine(board, state);

        engine.triggerJump(pos);

        assertEquals(PieceState.IDLE, pawn.getState(), "כאשר המשחק הסתיים, triggerJump אסור שישנה את מצב הכלי");
    }

    @Test
    void triggerJumpDoesNothingForAirbornePiece() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position pos = new Position(2, 2);
        Piece pawn = new Piece("p", PieceColor.WHITE, PieceType.PAWN, pos);
        pawn.setState(PieceState.AIRBORNE);
        board.addPiece(pos, pawn);
        GameEngine engine = newEngine(board, state);

        engine.triggerJump(pos);

        assertEquals(PieceState.AIRBORNE, pawn.getState(), "כלי שנמצא כבר באוויר אינו אמור לעבור למצב JUMPING");
    }

    @Test
    void jumpProtectedPieceCausesAttackerToBeCapturedInstead() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        Piece attacker = new Piece("attacker", PieceColor.WHITE, PieceType.ROOK, src);
        Piece defender = new Piece("defender", PieceColor.BLACK, PieceType.PAWN, dest);
        defender.setState(PieceState.JUMPING);
        defender.setJumpExpiryTime(5000L);
        board.addPiece(src, attacker);
        board.addPiece(dest, defender);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, dest); // זמן הגעה = 1000, עדיין לפני תום ההגנה (5000)
        engine.waitMs(1000L);

        assertNull(board.getPieceAt(src), "התוקף חייב להיעלם מהמשבצת המקורית אחרי שנתפס באוויר");
        assertEquals(defender, board.getPieceAt(dest), "המגן המוגן על ידי קפיצה חייב להישאר במקומו");
        assertEquals(PieceState.CAPTURED, attacker.getState(), "התוקף חייב לעבור למצב CAPTURED");
        assertFalse(state.isGameOver(), "תפיסה כזו אינה אמורה לסיים את המשחק");
    }

    @Test
    void pawnPromotesUponReachingLastRow() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(1, 0);
        Position dest = new Position(0, 0);
        Piece pawn = new Piece("wp", PieceColor.WHITE, PieceType.PAWN, src);
        board.addPiece(src, pawn);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, dest);
        engine.waitMs(1000L);

        assertEquals(PieceType.QUEEN, pawn.getType(), "רגלי שמגיע לשורה האחרונה חייב להיות מוכתר למלכה");
    }
}