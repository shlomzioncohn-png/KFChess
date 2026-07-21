package server;

import bus.EventListener;
import bus.events.GameOverEvent;
import org.java_websocket.WebSocket;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RematchService implements EventListener {

    private static final int RETURN_TO_QUEUE_SECONDS = 5;

    private final PlayerRegistry playerRegistry;
    private final ScheduledExecutorService scheduler;
    private final Runnable onReturnToQueue;

    public RematchService(PlayerRegistry playerRegistry, ScheduledExecutorService scheduler, Runnable onReturnToQueue) {
        this.playerRegistry = playerRegistry;
        this.scheduler = scheduler;
        this.onReturnToQueue = onReturnToQueue;
    }

    @Override
    public void onEvent(String topic, Object payload) {
        if (!(payload instanceof GameOverEvent)) {
            return;
        }

        WebSocket whiteConn = playerRegistry.findConnectionByRole(PlayerRole.WHITE);
        WebSocket blackConn = playerRegistry.findConnectionByRole(PlayerRole.BLACK);

        sendQuiet(whiteConn, "RETURN_TO_QUEUE_COUNTDOWN " + RETURN_TO_QUEUE_SECONDS);
        sendQuiet(blackConn, "RETURN_TO_QUEUE_COUNTDOWN " + RETURN_TO_QUEUE_SECONDS);

        scheduler.schedule(() -> returnToQueue(whiteConn, blackConn), RETURN_TO_QUEUE_SECONDS, TimeUnit.SECONDS);
    }

    private void returnToQueue(WebSocket whiteConn, WebSocket blackConn) {
        if (whiteConn != null) {
            playerRegistry.removeRole(whiteConn);
        }
        if (blackConn != null) {
            playerRegistry.removeRole(blackConn);
        }

        sendQuiet(whiteConn, "RETURN_TO_QUEUE");
        sendQuiet(blackConn, "RETURN_TO_QUEUE");

        onReturnToQueue.run();
    }

    private void sendQuiet(WebSocket conn, String message) {
        if (conn == null) {
            return;
        }
        try {
            conn.send(message);
        } catch (Exception e) {
            System.out.println("[REMATCH] could not send to a player: " + e.getMessage());
        }
    }
}
