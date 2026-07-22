package server;

import bus.EventListener;
import bus.events.CaptureEvent;
import bus.events.GameOverEvent;
import bus.events.JumpEvent;
import bus.events.MoveEvent;

public class NetworkBroadcastSubscriber implements EventListener {

    private final GameSession session;

    public NetworkBroadcastSubscriber(GameSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (payload instanceof MoveEvent e) {
            session.broadcastToMembers("MOVE " + e.getSource().getRow() + "," + e.getSource().getCol()
                    + " " + e.getDestination().getRow() + "," + e.getDestination().getCol());
        } else if (payload instanceof CaptureEvent e) {
            session.broadcastToMembers("CAPTURE " + e.getDestination().getRow() + "," + e.getDestination().getCol());
        } else if (payload instanceof GameOverEvent e) {
            session.broadcastToMembers("GAMEOVER " + e.getWinner());
        } else if (payload instanceof JumpEvent e) {
            session.broadcastToMembers("JUMP " + e.getPosition().getRow() + "," + e.getPosition().getCol());
        }
    }
}
