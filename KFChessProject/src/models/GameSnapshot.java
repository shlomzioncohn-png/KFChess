package models;


/**
 * GameSnapshot מייצג תמונת מצב קפואה ובלתי ניתנת לשינוי (Immutable) של המשחק.
 */
public class GameSnapshot {

    private final Board board;

    private final GameState gameState;


    public GameSnapshot(Board board, GameState gameState) {
        this.board = board;
        this.gameState = gameState;
    }

    


    public Board getBoard() {
        return board;
    }

    public GameState getGameState() {
        return gameState;
    }
}