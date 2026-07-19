//package tests;
//
//
//import engine.GameEngine;
//import io.BoardParser;
//import models.Board;
//import models.GameState;
//import models.Position;
//import org.junit.jupiter.api.Test;
//import realtime.RealTimeArbiter;
//import view.RenderSnapshot;
//import view.SnapshotFactory;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class SnapshotTest {
//
//    @Test
//    public void snapshotContainsCorrectPieceCount() {
//        Board board = BoardParser.parse("wR . . bK\n.  .  .  .\n.  .  .  .\n.  .  .  .");
//        RealTimeArbiter arbiter = new RealTimeArbiter();
//        GameState gameState = new GameState();
//        GameEngine engine = new GameEngine(board, arbiter, gameState);
//
//        RenderSnapshot snapshot = SnapshotFactory.build(
//                board, engine, arbiter, null, 100, 0L, gameState, "W", "B");
//
//        assertEquals(2, snapshot.pieces().size());
//    }
//
//    @Test
//    public void renderingDoesNotMutateGame() {
//        Board board = BoardParser.parse("wR . . bK\n.  .  .  .\n.  .  .  .\n.  .  .  .");
//        RealTimeArbiter arbiter = new RealTimeArbiter();
//        GameState gameState = new GameState();
//        GameEngine engine = new GameEngine(board, arbiter, gameState);
//
//        SnapshotFactory.build(board, engine, arbiter, null, 100, 0L, gameState, "W", "B");
//        int scoreBefore = gameState.getScore(models.enums.PieceColor.WHITE);
//        SnapshotFactory.build(board, engine, arbiter, null, 100, 0L, gameState, "W", "B");
//        assertEquals(scoreBefore, gameState.getScore(models.enums.PieceColor.WHITE));
//    }
//}