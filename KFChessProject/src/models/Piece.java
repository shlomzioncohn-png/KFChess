package models;


import models.enums.PieceColor;
import models.enums.PieceState;
import models.enums.PieceType;

/**
 * Piece מייצג כלי שחמט.
 */
public class Piece {
    private final String id;
    private final PieceColor color;
    private final PieceType type;

    private Position cell;
    private PieceState state;


    public Piece(String id, PieceColor color, PieceType type, Position initialCell) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.cell = initialCell;
        this.state = PieceState.IDLE;
    }


    public String getId() {
        return id;
    }

    public PieceColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public Position getCell() {
        return cell;
    }

    public void setCell(Position cell) {
        this.cell = cell;
    }

    public PieceState getState() {
        return state;
    }

    public void setState(PieceState state) {
        this.state = state;
    }

    // לצרכי דיבאג והדפסה
    @Override
    public String toString() {
        return id + "[" + type + "," + color + "," + state + " at " + cell + "]";
    }
}
