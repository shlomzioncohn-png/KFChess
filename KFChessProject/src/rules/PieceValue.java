package rules;

import models.enums.PieceType;

public class PieceValue {

    public static int getValue(PieceType type) {
        return switch (type) {
            case PAWN -> 1;
            case KNIGHT -> 3;
            case BISHOP -> 3;
            case ROOK -> 5;
            case QUEEN -> 9;
            case KING -> 0; // מלך לא נתפס בפועל - המשחק נגמר קודם
        };
    }
}