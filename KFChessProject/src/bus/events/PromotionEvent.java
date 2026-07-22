package bus.events;

import models.Piece;

public class PromotionEvent {

    private final Piece piece;

    public PromotionEvent(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }
}
