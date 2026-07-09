package models;


import models.enums.PieceColor;

/**
 * GameState מייצג את מצב המשחק הגלובלי ברגע נתון.
 */
public class GameState {

    private boolean gameOver;

    private PieceColor winner;

    public GameState() {
        this.gameOver = false;
        this.winner = null;
    }



    public boolean isGameOver() {
        return gameOver;
    }


    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }


    public PieceColor getWinner() {
        return winner;
    }


    public void setWinner(PieceColor winner) {
        this.winner = winner;
    }

    // ייצוג קריא של מצב המשחק לצרכי בדיקות והדפסה בלוגים
    @Override
    public String toString() {
        if (gameOver) {
            return "Game Over! Winner: " + (winner != null ? winner : "Draw");
        }
        return "Game is active";
    }
}