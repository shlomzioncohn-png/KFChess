package rules;

import models.Board;
import models.Position;

/**
 * היא משתמשת בעיקרון ההרכבה (Composition) –  יוצרת מופעים פנימיים של חוקי הצריח וחוקי הרץ,
 * ומאשרת את המהלך אם הוא חוקי עבור לפחות אחד מהם:
 */
public class QueenRule implements PieceRule{

    private final PieceRule rookRule = new RookRule();
    private final PieceRule bishopRule = new BishopRule();

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        return rookRule.isValidMove(board, from, to) || bishopRule.isValidMove(board, from, to);
    }

}
