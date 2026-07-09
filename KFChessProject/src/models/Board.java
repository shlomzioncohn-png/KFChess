package models;

/**
 * הממשק Board מגדיר את החוזה של הלוח הלוגי.
 */
public interface Board {
    int getWidth();
    int getHeight();

    Piece getPieceAt(Position pos);
    void addPiece(Position pos, Piece piece);
    void removePiece(Position pos);

    boolean isValidPosition(Position pos);
    void movePiece(Position src, Position dest);
}