package server;

import bus.EventListener;
import bus.events.CaptureEvent;
import bus.events.GameOverEvent;
import bus.events.JumpEvent;
import bus.events.MoveEvent;
import org.java_websocket.server.WebSocketServer;

public class NetworkBroadcastSubscriber implements EventListener {

    private final WebSocketServer server;

    public NetworkBroadcastSubscriber(WebSocketServer server) {
        this.server = server;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (payload instanceof MoveEvent e) {
            server.broadcast("MOVE " + e.getSource().getRow() + "," + e.getSource().getCol()
                    + " " + e.getDestination().getRow() + "," + e.getDestination().getCol());
        } else if (payload instanceof CaptureEvent e) {
            server.broadcast("CAPTURE " + e.getDestination().getRow() + "," + e.getDestination().getCol());
        } else if (payload instanceof GameOverEvent e) {
            server.broadcast("GAMEOVER " + e.getWinner());
        } else if (payload instanceof JumpEvent e) {
            server.broadcast("JUMP " + e.getPosition().getRow() + "," + e.getPosition().getCol());
        }
    }
}