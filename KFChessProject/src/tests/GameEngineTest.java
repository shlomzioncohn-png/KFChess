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
import realtime.Motion;
import realtime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {

    private GameEngine newEngine(Board board, GameState gameState) {
        return new GameEngine(board, new RealTimeArbiter(), gameState, new bus.EventBus());
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
    void updateAfterArrivalTimeMovesPieceAndEntersLongRestThenReturnsToIdle() {
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
        // מיד עם ההגעה הכלי נכנס למנוחה ארוכה (LONG_RESTING) - לא חוזר ישר ל-IDLE
        assertEquals(PieceState.LONG_RESTING, rook.getState(), "מיד לאחר סיום מהלך הכלי חייב להיכנס למנוחה ארוכה");

        engine.waitMs(GameEngine.LONG_REST_DURATION);
        assertEquals(PieceState.IDLE, rook.getState(), "אחרי שתם משך המנוחה הארוכה, הכלי חייב לחזור ל-IDLE");
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

    @Test
    void forceMoveSkipsRuleValidationAndAllowsAnIllegalMove() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(6, 6); // אלכסון - לא חוקי לצריח לפי RuleEngine
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, rook);
        GameEngine engine = newEngine(board, state);

        engine.forceMove(src, dest);

        assertEquals(PieceState.AIRBORNE, rook.getState(), "forceMove חייב להעביר את הכלי ל-AIRBORNE גם למהלך שאינו חוקי לפי חוקי המשחק");
        List<Motion> motions = engine.getActiveMotions();
        assertEquals(1, motions.size());
        assertEquals(dest, motions.get(0).getDestination(), "forceMove חייב ליצור תנועה בדיוק ליעד שסופק, בלי ולידציה חוזרת");
    }

    @Test
    void forceMoveOnEmptySourceDoesNothingSilently() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        GameEngine engine = newEngine(board, state);

        assertDoesNotThrow(() -> engine.forceMove(new Position(0, 0), new Position(1, 1)),
                "forceMove ממשבצת ריקה (דה-סינק) אסור לזרוק חריגה");
        assertTrue(engine.getActiveMotions().isEmpty());
    }

    @Test
    void forceMoveReplacesAnyExistingActiveMotionForTheSamePiece() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position oldDest = new Position(4, 5);   // tryMove: מרחק 1 -> הגעה ב-1000
        Position newDest = new Position(4, 7);   // forceMove: מרחק 3 -> הגעה ב-3000
        Piece rook = new Piece("r", PieceColor.WHITE, PieceType.ROOK, src);
        board.addPiece(src, rook);
        GameEngine engine = newEngine(board, state);

        engine.tryMove(src, oldDest);
        engine.forceMove(src, newDest);

        assertEquals(1, engine.getActiveMotions().size(), "forceMove חייב להחליף את התנועה הישנה, לא להוסיף תנועה שנייה");

        engine.waitMs(1000L); // הזמן שבו התנועה הישנה (ל-oldDest) הייתה אמורה להסתיים
        assertNull(board.getPieceAt(oldDest), "התנועה הישנה בוטלה - היעד הישן חייב להישאר ריק");
        assertEquals(rook, board.getPieceAt(src), "הכלי חייב עדיין להיות בטיסה (טרם הגיע ליעד החדש)");

        engine.waitMs(2000L); // עוד 2000, סה"כ 3000 - זמן ההגעה של forceMove
        assertEquals(rook, board.getPieceAt(newDest), "הכלי חייב להגיע ליעד החדש שנקבע ב-forceMove");
    }

    @Test
    void forceJumpSkipsValidationAndWorksEvenWhilePieceIsResting() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position pos = new Position(2, 2);
        Piece pawn = new Piece("p", PieceColor.WHITE, PieceType.PAWN, pos);
        pawn.setState(PieceState.LONG_RESTING);
        pawn.setRestExpiryTime(10_000L); // עדיין במנוחה זמן רב
        board.addPiece(pos, pawn);
        GameEngine engine = newEngine(board, state);

        engine.forceJump(pos);

        assertEquals(PieceState.JUMPING, pawn.getState(),
                "forceJump חייב לעבוד גם על כלי במנוחה - שלא כמו triggerJump הרגיל שהיה מסרב");
    }

    @Test
    void forceJumpOnEmptySourceDoesNothingSilently() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        GameEngine engine = newEngine(board, state);

        assertDoesNotThrow(() -> engine.forceJump(new Position(3, 3)),
                "forceJump על משבצת ריקה (דה-סינק) אסור לזרוק חריגה");
    }

    @Test
    void triggerJumpRefusesWhilePieceIsRestingUnlikeForceJump() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position pos = new Position(2, 2);
        Piece pawn = new Piece("p", PieceColor.WHITE, PieceType.PAWN, pos);
        pawn.setState(PieceState.LONG_RESTING);
        pawn.setRestExpiryTime(10_000L);
        board.addPiece(pos, pawn);
        GameEngine engine = newEngine(board, state);

        engine.triggerJump(pos);

        assertEquals(PieceState.LONG_RESTING, pawn.getState(),
                "triggerJump (בניגוד ל-forceJump) חייב לסרב לכלי שנמצא במנוחה");
    }

    @Test
    void decisiveCapturePublishesCaptureThenMoveThenGameOverInOrder() {
        Board board = new MatrixBoard(8, 8);
        GameState state = new GameState();
        Position src = new Position(4, 4);
        Position dest = new Position(4, 5);
        Piece rook = new Piece("attacker", PieceColor.WHITE, PieceType.ROOK, src);
        Piece enemyKing = new Piece("king", PieceColor.BLACK, PieceType.KING, dest);
        board.addPiece(src, rook);
        board.addPiece(dest, enemyKing);
        GameEngine engine = newEngine(board, state);

        List<String> publishedTopics = new ArrayList<>();
        engine.getBus().subscribe("piece.captured", (topic, payload) -> publishedTopics.add(topic));
        engine.getBus().subscribe("move.completed", (topic, payload) -> publishedTopics.add(topic));
        engine.getBus().subscribe("game.over", (topic, payload) -> publishedTopics.add(topic));

        engine.tryMove(src, dest);
        engine.waitMs(1000L);

        assertEquals(List.of("piece.captured", "move.completed", "game.over"), publishedTopics,
                "בתפיסה מכרעת, סדר האירועים חייב להיות: תפיסה, ואז מהלך הושלם, ואז סוף משחק - ולא הפוך");
    }
}