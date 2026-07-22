package server;

import bus.EventListener;
import bus.events.CaptureEvent;
import bus.events.GameOverEvent;
import bus.events.JumpEvent;
import bus.events.MoveEvent;

/**
 * כותב לטבלת activity_log כל אירוע משמעותי (מהלך, תפיסה, קפיצה, סוף משחק) עם roomId וזמן.
 * per-session - כל GameSession נרשם עם ה-roomId שלו.
 */
public class ActivityLogSubscriber implements EventListener {

    private final String roomId;

    public ActivityLogSubscriber(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (payload instanceof MoveEvent e) {
            DatabaseManager.logEvent(roomId, "MOVE",
                    e.getSource().getRow() + "," + e.getSource().getCol()
                            + " -> " + e.getDestination().getRow() + "," + e.getDestination().getCol());
        } else if (payload instanceof CaptureEvent e) {
            DatabaseManager.logEvent(roomId, "CAPTURE",
                    e.getAttacker().getType() + " captured " + e.getCaptured().getType());
        } else if (payload instanceof JumpEvent e) {
            DatabaseManager.logEvent(roomId, "JUMP", e.getPosition().getRow() + "," + e.getPosition().getCol());
        } else if (payload instanceof GameOverEvent e) {
            DatabaseManager.logEvent(roomId, "GAME_OVER", "winner=" + e.getWinner());
        }
    }
}
