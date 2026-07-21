package server;

import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerRegistry {

    private final Map<WebSocket, PlayerRole> roles = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> names = new ConcurrentHashMap<>();

    public void setName(WebSocket conn, String username) {
        names.put(conn, username);
    }

    public String getName(WebSocket conn) {
        return names.get(conn);
    }

    public void setRole(WebSocket conn, PlayerRole role) {
        roles.put(conn, role);
    }

    public PlayerRole getRole(WebSocket conn) {
        return roles.get(conn);
    }

    public boolean hasRole(WebSocket conn) {
        return roles.containsKey(conn);
    }

    public void removeRole(WebSocket conn) {
        roles.remove(conn);
    }

    public WebSocket findConnectionByRole(PlayerRole targetRole) {
        for (Map.Entry<WebSocket, PlayerRole> entry : roles.entrySet()) {
            if (entry.getValue() == targetRole) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getUsernameByRole(PlayerRole targetRole) {
        WebSocket conn = findConnectionByRole(targetRole);
        return conn == null ? null : names.get(conn);
    }
}
