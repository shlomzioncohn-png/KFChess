package server;

import bus.EventBus;
import bus.events.GameOverEvent;
import models.enums.PieceColor;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ReconnectManager {

    private static final int RESIGN_TIMEOUT_SECONDS = 20;

    private final PlayerRegistry playerRegistry;
    private final ScheduledExecutorService scheduler;
    private final Supplier<EventBus> busSupplier;
    private final Map<String, PendingDisconnect> pendingDisconnects = new ConcurrentHashMap<>();
    private volatile boolean pausedForDisconnect = false;

    private record PendingDisconnect(PlayerRole role, WebSocket deadConn, WebSocket opponentConn,
                                      ScheduledFuture<?> timeoutTask) {}

    public ReconnectManager(PlayerRegistry playerRegistry, ScheduledExecutorService scheduler, Supplier<EventBus> busSupplier) {
        this.playerRegistry = playerRegistry;
        this.scheduler = scheduler;
        this.busSupplier = busSupplier;
    }

    public boolean isPausedForDisconnect() {
        return pausedForDisconnect;
    }

    public void onPlayerDisconnected(WebSocket disconnectedConn, PlayerRole disconnectedRole) {
        String username = playerRegistry.getName(disconnectedConn);
        PlayerRole opponentRole = disconnectedRole == PlayerRole.WHITE ? PlayerRole.BLACK : PlayerRole.WHITE;
        WebSocket opponentConn = playerRegistry.findConnectionByRole(opponentRole);

        if (username == null || opponentConn == null) {
            playerRegistry.removeRole(disconnectedConn);
            return;
        }

        if (!opponentConn.isOpen()) {
            System.out.println("[RECONNECT] both players disconnected - clearing match, no resign/no rating change");
            cancelPendingDisconnect(playerRegistry.getName(opponentConn));
            playerRegistry.removeRole(disconnectedConn);
            playerRegistry.removeRole(opponentConn);
            pausedForDisconnect = false;
            return;
        }

        pausedForDisconnect = true;

        try {
            opponentConn.send("OPPONENT_DISCONNECTED " + RESIGN_TIMEOUT_SECONDS);
        } catch (Exception e) {
            System.out.println("[RECONNECT] could not notify opponent of disconnect: " + e.getMessage());
        }

        ScheduledFuture<?> task = scheduler.schedule(
                () -> handleResignTimeout(username),
                RESIGN_TIMEOUT_SECONDS, TimeUnit.SECONDS
        );

        pendingDisconnects.put(username, new PendingDisconnect(disconnectedRole, disconnectedConn, opponentConn, task));
    }

    public PlayerRole tryReconnect(String username, WebSocket newConn) {
        PendingDisconnect pending = pendingDisconnects.remove(username);
        if (pending == null) {
            return null;
        }

        pending.timeoutTask().cancel(false);
        playerRegistry.removeRole(pending.deadConn());
        playerRegistry.setRole(newConn, pending.role());
        pausedForDisconnect = false;
        try {
            pending.opponentConn().send("OPPONENT_RECONNECTED");
        } catch (Exception e) {
            System.out.println("[RECONNECT] could not notify opponent of reconnect: " + e.getMessage());
        }
        System.out.println("[RECONNECT] " + username + " reconnected in time - resuming as " + pending.role());
        return pending.role();
    }

    private void cancelPendingDisconnect(String username) {
        if (username == null) {
            return;
        }
        PendingDisconnect pending = pendingDisconnects.remove(username);
        if (pending != null) {
            pending.timeoutTask().cancel(false);
        }
    }

    private void handleResignTimeout(String username) {
        PendingDisconnect pending = pendingDisconnects.remove(username);
        if (pending == null) {
            return;
        }

        pausedForDisconnect = false;
        PieceColor winnerColor = pending.role() == PlayerRole.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        System.out.println("[RECONNECT] " + username + " did not reconnect in time - auto-resign");
        busSupplier.get().publish("game.over", new GameOverEvent(winnerColor));

        playerRegistry.removeRole(pending.deadConn());
    }
}
