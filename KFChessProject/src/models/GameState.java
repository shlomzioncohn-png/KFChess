package models;


import models.enums.PieceColor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * GameState מייצג את מצב המשחק הגלובלי ברגע נתון.
 */
public class GameState {

    private boolean gameOver;

    private PieceColor winner;

    private final Map<PieceColor, Integer> scores = new EnumMap<>(PieceColor.class);

    private final List<String> moveLog = new ArrayList<>();

    public GameState() {
        this.gameOver = false;
        this.winner = null;
        scores.put(PieceColor.WHITE, 0);
        scores.put(PieceColor.BLACK, 0);
    }

    public int getScore(PieceColor color) {
        return scores.get(color);
    }

    public void addScore(PieceColor color, int points) {
        scores.merge(color, points, Integer::sum);
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

    public void addLogEntry(String entry) {
        moveLog.add(entry);
    }

    public List<String> getMoveLog() {
        return List.copyOf(moveLog);
    }


}