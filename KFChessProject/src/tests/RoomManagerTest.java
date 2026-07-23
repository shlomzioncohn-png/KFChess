package  tests;

import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import server.DatabaseManager;
import server.GameSession;
import server.PlayerRole;
import server.RoomManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class RoomManagerTest {

    private RoomManager newRoomManager() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        return new RoomManager(scheduler);
    }

    private String captureCreatedRoomId(WebSocket conn) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(conn, atLeastOnce()).send(captor.capture());
        return captor.getAllValues().stream()
                .filter(m -> m.startsWith("ROOM_CREATED "))
                .findFirst()
                .map(m -> m.substring("ROOM_CREATED ".length()))
                .orElseThrow(() -> new AssertionError("ROOM_CREATED לא נשלח"));
    }

    @Test
    void createRoomThenJoinRoomByIdAssignsRolesInOrderWhiteThenBlackThenSpectator() {
        RoomManager rm = newRoomManager();
        WebSocket conn1 = mock(WebSocket.class);
        WebSocket conn2 = mock(WebSocket.class);
        WebSocket conn3 = mock(WebSocket.class);

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            rm.registerLogin(conn1, "alice");
            rm.createRoom(conn1);
            verify(conn1).send("ROLE WHITE");
            assertEquals(PlayerRole.WHITE, rm.getRoomForConnection(conn1).getPlayerRegistry().getRole(conn1));
            String roomId = captureCreatedRoomId(conn1);

            rm.registerLogin(conn2, "bob");
            rm.joinRoomById(conn2, roomId);
            verify(conn2).send("ROLE BLACK");
            assertEquals(PlayerRole.BLACK, rm.getRoomForConnection(conn2).getPlayerRegistry().getRole(conn2));

            rm.registerLogin(conn3, "carol");
            rm.joinRoomById(conn3, roomId);
            verify(conn3).send("ROLE SPECTATOR");
            assertEquals(PlayerRole.SPECTATOR, rm.getRoomForConnection(conn3).getPlayerRegistry().getRole(conn3),
                    "אחרי שהתפקידים WHITE ו-BLACK תפוסים, מצטרף שלישי חייב לקבל SPECTATOR");
        }
    }

    @Test
    void joinRoomByIdWithNonexistentRoomSendsJoinFailedAndCreatesNoSession() {
        RoomManager rm = newRoomManager();
        WebSocket conn = mock(WebSocket.class);

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            rm.registerLogin(conn, "dave");
            rm.joinRoomById(conn, "NOSUCH");

            verify(conn, times(1)).send("JOIN_FAILED room not found");
            assertEquals("default", rm.getRoomForConnection(conn).getRoomId(),
                    "ניסיון הצטרפות שנכשל אסור להעביר את השחקן מהלובי - הוא חייב להישאר ב-default session");
        }
    }

    @Test
    void leavingRoomWhenItBecomesEmptyRemovesItFromDiscoverableRooms() {
        RoomManager rm = newRoomManager();
        WebSocket conn1 = mock(WebSocket.class);
        WebSocket lateJoiner = mock(WebSocket.class);

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            rm.registerLogin(conn1, "alice");
            rm.createRoom(conn1);
            String roomId = captureCreatedRoomId(conn1);

            rm.leaveCurrentRoom(conn1); // היחיד בחדר עוזב - החדר אמור להתנקות

            rm.registerLogin(lateJoiner, "zoe");
            rm.joinRoomById(lateJoiner, roomId);

            verify(lateJoiner, times(1)).send("JOIN_FAILED room not found");
            verify(lateJoiner, never()).send(startsWith("ROLE "));
        }
    }

    @Test
    void tryReconnectAnywhereFindsPendingDisconnectInNonDefaultSessionAndRestoresRole() {
        RoomManager rm = newRoomManager();
        WebSocket whiteConn = mock(WebSocket.class);
        WebSocket blackConn = mock(WebSocket.class);
        WebSocket newWhiteConn = mock(WebSocket.class);
        when(blackConn.isOpen()).thenReturn(true);

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            rm.registerLogin(whiteConn, "alice");
            rm.createRoom(whiteConn);
            String roomId = captureCreatedRoomId(whiteConn);
            rm.registerLogin(blackConn, "bob");
            rm.joinRoomById(blackConn, roomId);

            rm.handleDisconnect(whiteConn); // alice (WHITE) מתנתקת, bob פתוח - נכנס למצב ממתין ל-reconnect

            PlayerRole restoredRole = rm.tryReconnectAnywhere("alice", newWhiteConn);

            assertEquals(PlayerRole.WHITE, restoredRole, "reconnect דרך RoomManager חייב למצוא את הניתוק בכל ה-sessions הפעילים, לא רק ב-default");
            verify(newWhiteConn, atLeastOnce()).send(startsWith("BOARD_STATE\n"));
            assertEquals(rm.getRoomForConnection(blackConn), rm.getRoomForConnection(newWhiteConn),
                    "השחקן שהתחבר מחדש חייב לחזור לאותו room שבו שיחק");
        }
    }

    @Test
    void tryReconnectAnywhereReturnsNullWhenNoSessionHasAPendingDisconnectForThatUsername() {
        RoomManager rm = newRoomManager();

        PlayerRole result = rm.tryReconnectAnywhere("nobody-was-disconnected", mock(WebSocket.class));

        assertNull(result);
    }
}
