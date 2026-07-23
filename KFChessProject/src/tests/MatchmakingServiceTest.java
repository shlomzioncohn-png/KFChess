
package  tests;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import server.DatabaseManager;
import server.MatchmakingService;
import server.RoomManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MatchmakingServiceTest {

    private RoomManager roomManager;
    private ScheduledExecutorService scheduler;
    private MatchmakingService service;

    private void setUp() {
        roomManager = mock(RoomManager.class);
        scheduler = mock(ScheduledExecutorService.class);
        service = new MatchmakingService(roomManager, scheduler);
    }

    @Test
    void matchesTwoPlayersWithinRatingRangeAndCreatesRoom() {
        setUp();
        WebSocket connA = mock(WebSocket.class);
        WebSocket connB = mock(WebSocket.class);
        when(roomManager.getUsername(connA)).thenReturn("alice");
        when(roomManager.getUsername(connB)).thenReturn("bob");

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            db.when(() -> DatabaseManager.getRating("alice")).thenReturn(1200);
            db.when(() -> DatabaseManager.getRating("bob")).thenReturn(1250); // הפרש 50, בתוך הטווח (100)

            service.handlePlayRequest(connA); // אין עדיין יריב - נכנס לתור
            service.handlePlayRequest(connB); // רואה את alice בתור, בטווח הדירוג - מתאים

            verify(roomManager, times(1)).createMatchmakingRoom(connB, connA);
        }
    }

    @Test
    void doesNotMatchPlayersOutsideRatingRange() {
        setUp();
        WebSocket connA = mock(WebSocket.class);
        WebSocket connC = mock(WebSocket.class);
        when(roomManager.getUsername(connA)).thenReturn("alice");
        when(roomManager.getUsername(connC)).thenReturn("carol");

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            db.when(() -> DatabaseManager.getRating("alice")).thenReturn(1200);
            db.when(() -> DatabaseManager.getRating("carol")).thenReturn(1350); // הפרש 150, מחוץ לטווח

            service.handlePlayRequest(connA);
            service.handlePlayRequest(connC);

            verify(roomManager, never()).createMatchmakingRoom(any(), any());
            verify(scheduler, times(2)).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    void playerNotLoggedInReceivesPlayFailedAndIsNeverQueued() {
        setUp();
        WebSocket conn = mock(WebSocket.class);
        when(roomManager.getUsername(conn)).thenReturn(null);

        service.handlePlayRequest(conn);

        verify(conn, times(1)).send("PLAY_FAILED not logged in");
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any());
        verify(roomManager, never()).createMatchmakingRoom(any(), any());
    }

    @Test
    void playerWithActiveRoleIsIgnored() {
        setUp();
        WebSocket conn = mock(WebSocket.class);
        when(roomManager.getUsername(conn)).thenReturn("alice");
        when(roomManager.hasActiveRole(conn)).thenReturn(true);

        service.handlePlayRequest(conn);

        verify(conn, never()).send(anyString());
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    void playerCannotEnterTheQueueTwice() {
        setUp();
        WebSocket conn = mock(WebSocket.class);
        when(roomManager.getUsername(conn)).thenReturn("alice");

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            db.when(() -> DatabaseManager.getRating("alice")).thenReturn(1200);

            service.handlePlayRequest(conn); // נכנס לתור בפעם הראשונה
            service.handlePlayRequest(conn); // ניסיון שני - כבר בתור

            verify(scheduler, times(1)).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    void removeFromQueuePreventsFutureMatchWithThatPlayer() {
        setUp();
        WebSocket connA = mock(WebSocket.class);
        WebSocket connB = mock(WebSocket.class);
        when(roomManager.getUsername(connA)).thenReturn("alice");
        when(roomManager.getUsername(connB)).thenReturn("bob");

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            db.when(() -> DatabaseManager.getRating("alice")).thenReturn(1200);
            db.when(() -> DatabaseManager.getRating("bob")).thenReturn(1200);

            service.handlePlayRequest(connA);
            service.removeFromQueue(connA);
            service.handlePlayRequest(connB);

            verify(roomManager, never()).createMatchmakingRoom(any(), any());
            // A כבר לא בתור, אז B לא מוצא יריב ומצטרף בעצמו לתור - 2 תזמוני timeout בסך הכל
            // (זה של A מהצטרפותו המקורית, שמעולם לא בוטל ע"י removeFromQueue, ועוד אחד חדש עבור B)
            verify(scheduler, times(2)).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    void matchTimeoutSendsNoMatchWhenPlayerIsStillWaiting() {
        setUp();
        WebSocket conn = mock(WebSocket.class);
        when(roomManager.getUsername(conn)).thenReturn("alice");
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            db.when(() -> DatabaseManager.getRating("alice")).thenReturn(1200);
            service.handlePlayRequest(conn);
        }
        verify(scheduler).schedule(captor.capture(), eq(60L), eq(TimeUnit.SECONDS));

        captor.getValue().run(); // מדמה שהזמן חלף, בלי לחכות באמת 60 שניות

        verify(conn, times(1)).send("NO_MATCH");
    }

    @Test
    void matchTimeoutDoesNothingIfPlayerWasAlreadyMatchedBeforeItFired() {
        setUp();
        WebSocket connA = mock(WebSocket.class);
        WebSocket connB = mock(WebSocket.class);
        when(roomManager.getUsername(connA)).thenReturn("alice");
        when(roomManager.getUsername(connB)).thenReturn("bob");
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        try (MockedStatic<DatabaseManager> db = mockStatic(DatabaseManager.class)) {
            db.when(() -> DatabaseManager.getRating("alice")).thenReturn(1200);
            db.when(() -> DatabaseManager.getRating("bob")).thenReturn(1200);
            service.handlePlayRequest(connA); // alice נכנסת לתור, מתוזמן טיימאאוט
            service.handlePlayRequest(connB); // bob מתאים ל-alice ומוציא אותה מהתור
        }
        verify(scheduler).schedule(captor.capture(), eq(60L), eq(TimeUnit.SECONDS));

        captor.getValue().run(); // הטיימאאוט המקורי של alice יורה באיחור, אחרי שכבר הותאמה

        verify(connA, never()).send("NO_MATCH");
    }
}
