package  tests;

import bus.EventBus;
import bus.events.GameOverEvent;
import models.enums.PieceColor;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import server.PlayerRegistry;
import server.PlayerRole;
import server.ReconnectManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReconnectManagerTest {

    private PlayerRegistry playerRegistry;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;
    private EventBus bus;
    private List<GameOverEvent> gameOverEvents;
    private ReconnectManager manager;

    @SuppressWarnings("unchecked")
    private void setUp() {
        playerRegistry = mock(PlayerRegistry.class);
        scheduler = mock(ScheduledExecutorService.class);
        scheduledFuture = mock(ScheduledFuture.class);
        doReturn(scheduledFuture).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        bus = new EventBus();
        gameOverEvents = new ArrayList<>();
        bus.subscribe("game.over", (topic, payload) -> gameOverEvents.add((GameOverEvent) payload));
        manager = new ReconnectManager(playerRegistry, scheduler, () -> bus);
    }

    @Test
    void onPlayerDisconnectedWithUnknownUsernameJustRemovesRole() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn(null);

        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);

        verify(playerRegistry, times(1)).removeRole(deadConn);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any());
        assertFalse(manager.isPausedForDisconnect());
    }

    @Test
    void onPlayerDisconnectedWithNoOpponentJustRemovesRole() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn("alice");
        when(playerRegistry.findConnectionByRole(PlayerRole.BLACK)).thenReturn(null);

        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);

        verify(playerRegistry, times(1)).removeRole(deadConn);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    void onPlayerDisconnectedWithOpenOpponentNotifiesAndStartsResignTimer() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        WebSocket opponentConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn("alice");
        when(playerRegistry.findConnectionByRole(PlayerRole.BLACK)).thenReturn(opponentConn);
        when(opponentConn.isOpen()).thenReturn(true);

        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);

        assertTrue(manager.isPausedForDisconnect(), "המשחק חייב להיות מושהה כשיריב פתוח מחכה לחיבור מחדש");
        verify(opponentConn, times(1)).send("OPPONENT_DISCONNECTED 20");
        verify(scheduler, times(1)).schedule(any(Runnable.class), eq(20L), eq(TimeUnit.SECONDS));
    }

    @Test
    void bothPlayersDisconnectedClearsBothRolesWithoutStartingResignTimer() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        WebSocket opponentConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn("alice");
        when(playerRegistry.findConnectionByRole(PlayerRole.BLACK)).thenReturn(opponentConn);
        when(opponentConn.isOpen()).thenReturn(false); // גם היריב כבר מנותק

        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);

        verify(playerRegistry, times(1)).removeRole(deadConn);
        verify(playerRegistry, times(1)).removeRole(opponentConn);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any());
        verify(opponentConn, never()).send(anyString());
        assertFalse(manager.isPausedForDisconnect(), "שני שחקנים מנותקים - אסור להישאר במצב מושהה עם resign ממתין");
    }

    @Test
    void tryReconnectWithNoPendingDisconnectReturnsNull() {
        setUp();
        assertNull(manager.tryReconnect("nobody-waiting", mock(WebSocket.class)));
    }

    @Test
    void tryReconnectCancelsTimeoutRestoresRoleAndNotifiesOpponent() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        WebSocket opponentConn = mock(WebSocket.class);
        WebSocket newConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn("alice");
        when(playerRegistry.findConnectionByRole(PlayerRole.BLACK)).thenReturn(opponentConn);
        when(opponentConn.isOpen()).thenReturn(true);
        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);

        PlayerRole restoredRole = manager.tryReconnect("alice", newConn);

        assertEquals(PlayerRole.WHITE, restoredRole, "reconnect חייב להחזיר את התפקיד שהיה לשחקן לפני הניתוק");
        verify(scheduledFuture, times(1)).cancel(false);
        verify(playerRegistry, times(1)).removeRole(deadConn);
        verify(playerRegistry, times(1)).setRole(newConn, PlayerRole.WHITE);
        verify(opponentConn, times(1)).send("OPPONENT_RECONNECTED");
        assertFalse(manager.isPausedForDisconnect(), "אחרי reconnect מוצלח המשחק אסור שיישאר מושהה");
    }

    @Test
    void resignTimeoutDeclaresOpponentAsWinnerWhenPlayerNeverReconnects() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        WebSocket opponentConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn("alice");
        when(playerRegistry.findConnectionByRole(PlayerRole.BLACK)).thenReturn(opponentConn);
        when(opponentConn.isOpen()).thenReturn(true);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);
        verify(scheduler).schedule(captor.capture(), eq(20L), eq(TimeUnit.SECONDS));

        captor.getValue().run(); // מדמה חלוף 20 השניות בלי reconnect

        assertEquals(1, gameOverEvents.size());
        assertEquals(PieceColor.BLACK, gameOverEvents.get(0).getWinner(),
                "כשהלבן לא מתחבר מחדש בזמן, השחור (היריב) חייב לנצח ב-resign");
        verify(playerRegistry, times(1)).removeRole(deadConn);
        assertFalse(manager.isPausedForDisconnect());
    }

    @Test
    void resignTimeoutDoesNothingIfPlayerAlreadyReconnectedBeforeItFired() {
        setUp();
        WebSocket deadConn = mock(WebSocket.class);
        WebSocket opponentConn = mock(WebSocket.class);
        WebSocket newConn = mock(WebSocket.class);
        when(playerRegistry.getName(deadConn)).thenReturn("alice");
        when(playerRegistry.findConnectionByRole(PlayerRole.BLACK)).thenReturn(opponentConn);
        when(opponentConn.isOpen()).thenReturn(true);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        manager.onPlayerDisconnected(deadConn, PlayerRole.WHITE);
        verify(scheduler).schedule(captor.capture(), eq(20L), eq(TimeUnit.SECONDS));
        manager.tryReconnect("alice", newConn);

        captor.getValue().run(); // הטיימאאוט המקורי יורה באיחור, אחרי שכבר חזרה

        assertTrue(gameOverEvents.isEmpty(), "לאחר reconnect מוצלח, טיימאאוט מאוחר אסור לגרום resign");
    }
}
