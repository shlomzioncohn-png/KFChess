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
    private PieceType type;

    private Position cell;
    private PieceState state;

    private long jumpExpiryTime; // 0 = אין הגנת קפיצה פעילה

    private long restExpiryTime;



    public Piece(String id, PieceColor color, PieceType type, Position initialCell) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.cell = initialCell;
        this.state = PieceState.IDLE;
        this.jumpExpiryTime = 0;

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

    public void promote(PieceType newType) {
        this.type = newType;
    }

    public void setJumpExpiryTime(long jumpExpiryTime) {
        this.jumpExpiryTime = jumpExpiryTime;
    }

    /**
     * האם הכלי הזה, עכשיו, מוגן על ידי קפיצה פעילה.
     * הכלי הוא היחיד שיודע לענות על השאלה הזו על עצמו.
     */
    public boolean isProtectedByJump(long currentClock) {
        return state == PieceState.JUMPING && currentClock < jumpExpiryTime;
    }

    public long getJumpExpiryTime() {
        return jumpExpiryTime;
    }

    public void setRestExpiryTime(long restExpiryTime) {
        this.restExpiryTime = restExpiryTime;
    }

    public long getRestExpiryTime() {
        return restExpiryTime;
    }




    // לצרכי דיבאג והדפסה
    @Override
    public String toString() {
        return id + "[" + type + "," + color + "," + state + " at " + cell + "]";
    }
}
