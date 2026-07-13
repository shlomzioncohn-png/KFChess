package models;

/**
* מנהל את הלוח בלי לדעת מה קיים בו ומה החוקים
 */
public class MatrixBoard implements Board {

    private final int width;
    private final int height;

    private final Piece[][] matrix;


    public MatrixBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.matrix = new Piece[height][width];
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }


    @Override
    public boolean isValidPosition(Position pos) {
        if (pos == null) return false;
        return pos.getRow() >= 0 && pos.getRow() < height &&
                pos.getCol() >= 0 && pos.getCol() < width;
    }


    @Override
    public Piece getPieceAt(Position pos) {
        if (!isValidPosition(pos)) {
            return null; // מחוץ לגבולות הלוח
        }
        return matrix[pos.getRow()][pos.getCol()];
    }


    @Override
    public void addPiece(Position pos, Piece piece) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Position out of bounds");
        }


        if (getPieceAt(pos) != null) {
            throw new IllegalStateException("Double Occupancy Rejection: Cell " + pos + " is already occupied!");
        }
        matrix[pos.getRow()][pos.getCol()] = piece;
        piece.setCell(pos);
    }


    @Override
    public void removePiece(Position pos) {
        if (!isValidPosition(pos)) return;

        Piece piece = getPieceAt(pos);
        if (piece != null) {
            matrix[pos.getRow()][pos.getCol()] = null; // מנקה את התא במטריצה ל-null
        }
    }


    @Override
    public void movePiece(Position src, Position dest) {
        if (!isValidPosition(src) || !isValidPosition(dest)) {
            throw new IllegalArgumentException("Source or destination out of bounds");
        }

        Piece pieceToMove = getPieceAt(src);
        if (pieceToMove == null) {
            throw new IllegalStateException("No piece found at source position " + src);
        }

        matrix[src.getRow()][src.getCol()] = null;

        matrix[dest.getRow()][dest.getCol()] = pieceToMove;

        pieceToMove.setCell(dest);
    }
}