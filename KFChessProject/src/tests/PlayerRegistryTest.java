package  tests;

import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;
import server.PlayerRegistry;
import server.PlayerRole;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PlayerRegistryTest {

    @Test
    void setRoleThenGetRoleReturnsIt() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket conn = mock(WebSocket.class);

        registry.setRole(conn, PlayerRole.WHITE);

        assertEquals(PlayerRole.WHITE, registry.getRole(conn));
    }

    @Test
    void getRoleForUnknownConnectionReturnsNull() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket unknownConn = mock(WebSocket.class);

        assertNull(registry.getRole(unknownConn));
    }

    @Test
    void hasRoleReflectsPresenceBeforeAndAfterRemoval() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket conn = mock(WebSocket.class);

        assertFalse(registry.hasRole(conn), "לפני setRole אסור שיהיה תפקיד");
        registry.setRole(conn, PlayerRole.BLACK);
        assertTrue(registry.hasRole(conn), "אחרי setRole חייב להיות תפקיד");
        registry.removeRole(conn);
        assertFalse(registry.hasRole(conn), "אחרי removeRole אסור שיהיה תפקיד");
    }

    @Test
    void removeRoleClearsRoleButDoesNotClearName() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket conn = mock(WebSocket.class);
        registry.setName(conn, "alice");
        registry.setRole(conn, PlayerRole.WHITE);

        registry.removeRole(conn);

        assertNull(registry.getRole(conn), "התפקיד חייב להיעלם");
        assertEquals("alice", registry.getName(conn), "removeRole אסור שישפיע על השם השמור - אלו מפות נפרדות");
    }

    @Test
    void findConnectionByRoleReturnsNullWhenNoOneHoldsThatRole() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket conn = mock(WebSocket.class);
        registry.setRole(conn, PlayerRole.WHITE);

        assertNull(registry.findConnectionByRole(PlayerRole.BLACK),
                "אין אף חיבור עם התפקיד המבוקש - חייב להחזיר null, לא לזרוק שגיאה");
    }

    @Test
    void findConnectionByRoleReturnsNullOnEmptyRegistry() {
        PlayerRegistry registry = new PlayerRegistry();

        assertNull(registry.findConnectionByRole(PlayerRole.WHITE));
    }

    @Test
    void findConnectionByRoleWithMultipleConnectionsReturnsTheCorrectOne() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket whiteConn = mock(WebSocket.class);
        WebSocket blackConn = mock(WebSocket.class);
        WebSocket spectatorConn = mock(WebSocket.class);
        registry.setRole(whiteConn, PlayerRole.WHITE);
        registry.setRole(blackConn, PlayerRole.BLACK);
        registry.setRole(spectatorConn, PlayerRole.SPECTATOR);

        assertSame(whiteConn, registry.findConnectionByRole(PlayerRole.WHITE));
        assertSame(blackConn, registry.findConnectionByRole(PlayerRole.BLACK));
        assertSame(spectatorConn, registry.findConnectionByRole(PlayerRole.SPECTATOR));
    }

    @Test
    void getUsernameByRoleReturnsNameOfMatchingConnection() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket conn = mock(WebSocket.class);
        registry.setName(conn, "bob");
        registry.setRole(conn, PlayerRole.BLACK);

        assertEquals("bob", registry.getUsernameByRole(PlayerRole.BLACK));
    }

    @Test
    void getUsernameByRoleReturnsNullWhenNoConnectionHoldsThatRole() {
        PlayerRegistry registry = new PlayerRegistry();

        assertNull(registry.getUsernameByRole(PlayerRole.WHITE),
                "כשאין חיבור עם התפקיד - השם חייב להיות null, לא לזרוק NullPointerException");
    }

    @Test
    void setNameThenGetNameReturnsItAndUnknownConnectionReturnsNull() {
        PlayerRegistry registry = new PlayerRegistry();
        WebSocket known = mock(WebSocket.class);
        WebSocket unknown = mock(WebSocket.class);
        registry.setName(known, "carol");

        assertEquals("carol", registry.getName(known));
        assertNull(registry.getName(unknown));
    }
}
