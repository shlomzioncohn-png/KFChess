package server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GameServer extends WebSocketServer {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final RoomManager roomManager = new RoomManager(scheduler);
    private final MatchmakingService matchmaking;

    public GameServer(int port) {
        super(new InetSocketAddress(port));
        this.matchmaking = new MatchmakingService(roomManager, scheduler);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[SERVER] client connected: " + conn.getRemoteSocketAddress());
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        matchmaking.removeFromQueue(conn);
        roomManager.handleDisconnect(conn);

        System.out.println("[SERVER] client disconnected: " + conn.getRemoteSocketAddress()
                + " | code=" + code + " reason=" + reason + " remote=" + remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[SERVER] received: " + message);

        if (message.startsWith("LOGIN ")) {
            handleLogin(conn, message);
            return;
        }

        if (message.equals("PLAY")) {
            matchmaking.handlePlayRequest(conn);
            return;
        }

        if (message.equals("CREATE_ROOM")) {
            matchmaking.removeFromQueue(conn);
            roomManager.createRoom(conn);
            return;
        }

        if (message.startsWith("JOIN_ROOM ")) {
            matchmaking.removeFromQueue(conn);
            roomManager.joinRoomById(conn, message.substring("JOIN_ROOM ".length()).trim());
            return;
        }

        if (message.equals("CANCEL")) {
            matchmaking.removeFromQueue(conn);
            roomManager.leaveCurrentRoom(conn);
            conn.send("ROOM_CANCELLED");
            return;
        }

        GameSession session = roomManager.getRoomForConnection(conn);
        if (session == null) {
            System.out.println("[SERVER] message from connection with no room yet - ignoring: " + message);
            return;
        }

        session.handleGameMessage(conn, message);
    }

    private void handleLogin(WebSocket conn, String message) {
        String[] parts = message.substring(6).split(" ");
        if (parts.length != 2) {
            conn.send("LOGIN_FAILED bad format");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        boolean userExists = DatabaseManager.userExists(username);
        boolean success = userExists
                ? DatabaseManager.verifyLogin(username, password)
                : DatabaseManager.registerUser(username, password);

        if (!success) {
            conn.send("LOGIN_FAILED wrong password");
            System.out.println("[SERVER] login failed for " + username);
            return;
        }

        roomManager.registerLogin(conn, username);
        DatabaseManager.logEvent(null, "LOGIN", username);
        int rating = DatabaseManager.getRating(username);

        PlayerRole restoredRole = roomManager.tryReconnectAnywhere(username, conn);
        if (restoredRole != null) {
            System.out.println("[SERVER] " + username + " logged in (rating " + rating + ") - reconnected as " + restoredRole);
            conn.send("LOGIN_OK " + rating + " RECONNECTED " + restoredRole);
        } else {
            System.out.println("[SERVER] " + username + " logged in (rating " + rating + ")");
            conn.send("LOGIN_OK " + rating);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("[SERVER] error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("[SERVER] started successfully");
    }
}
