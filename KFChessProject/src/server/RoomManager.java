package server;

import org.java_websocket.WebSocket;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * מנהל את כל ה-GameSession-ים הפעילים בשרת (rooms), כולל ה-"default" (לובי matchmaking בלבד -
 * אף אחד לא משחק בתוכו בפועל), ומתרגם WebSocket -> GameSession שהוא שייך אליו. כל הלוגיקה
 * העסקית של create/join/leave room חיה כאן - לא ב-GameServer, שנשאר router בלבד.
 */
public class RoomManager {

    // בלי 0/O ו-1/I/l - נמנעים מתווים שקל להתבלבל ביניהם כשמקלידים/מכתיבים קוד room
    private static final String ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int ID_LENGTH = 6;

    private final ScheduledExecutorService scheduler;
    private final SecureRandom random = new SecureRandom();

    // rooms ניתנים-לגילוי דרך JOIN_ROOM (לא כולל rooms אנונימיים שנוצרו על ידי matchmaking)
    private final Map<String, GameSession> rooms = new ConcurrentHashMap<>();
    private final Map<WebSocket, GameSession> connectionRoom = new ConcurrentHashMap<>();

    // כל session פעיל שאי-פעם התחיל משחק (כולל אנונימיים) - לחיפוש reconnect בלבד
    private final Set<GameSession> allSessions = ConcurrentHashMap.newKeySet();

    // username הוא תכונה של החיבור עצמו (מוגדר פעם אחת ב-LOGIN), לא של room ספציפי -
    // אין תלות בזה ש-registry-ים per-session שונים "יזכרו" אותו זה מזה.
    private final Map<WebSocket, String> usernames = new ConcurrentHashMap<>();

    private final GameSession defaultSession;

    public RoomManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
        // defaultSession הוא לובי בלבד - עוגן לרישום שמות ולתור matchmaking, אף אחד לא
        // משחק בתוכו בפועל (כל משחק אמיתי חי ב-session נפרד), אז אין צורך ב-board/engine.
        this.defaultSession = new GameSession("default", true, scheduler);
    }

    public GameSession getDefaultSession() {
        return defaultSession;
    }

    public void registerLogin(WebSocket conn, String username) {
        usernames.put(conn, username);
        joinRoom(conn, defaultSession);
        defaultSession.getPlayerRegistry().setName(conn, username);
    }

    public String getUsername(WebSocket conn) {
        return usernames.get(conn);
    }

    public GameSession getRoomForConnection(WebSocket conn) {
        return connectionRoom.get(conn);
    }

    public boolean hasActiveRole(WebSocket conn) {
        GameSession session = connectionRoom.get(conn);
        return session != null && session.getPlayerRegistry().hasRole(conn);
    }

    public void createRoom(WebSocket conn) {
        leaveCurrentRoom(conn);

        GameSession session = newSession(generateRoomId(), false);
        rooms.put(session.getRoomId(), session);

        joinRoom(conn, session);
        session.getPlayerRegistry().setName(conn, usernames.get(conn));
        session.getPlayerRegistry().setRole(conn, PlayerRole.WHITE);

        conn.send("ROOM_CREATED " + session.getRoomId());
        conn.send("ROLE " + PlayerRole.WHITE);
        conn.send("BOARD_STATE\n" + session.serializeBoard());
        session.broadcastToMembers(session.buildPlayersMessage());
        DatabaseManager.logEvent(session.getRoomId(), "CREATE_ROOM", usernames.get(conn) + " as WHITE");
    }

    public void joinRoomById(WebSocket conn, String roomId) {
        GameSession session = rooms.get(roomId);
        if (session == null) {
            conn.send("JOIN_FAILED room not found");
            return;
        }

        leaveCurrentRoom(conn);

        PlayerRole role = session.getPlayerRegistry().findConnectionByRole(PlayerRole.BLACK) == null
                ? PlayerRole.BLACK
                : PlayerRole.SPECTATOR;

        joinRoom(conn, session);
        session.getPlayerRegistry().setName(conn, usernames.get(conn));
        session.getPlayerRegistry().setRole(conn, role);

        conn.send("ROOM_JOINED " + roomId);
        conn.send("ROLE " + role);
        conn.send("BOARD_STATE\n" + session.serializeBoard());
        session.broadcastToMembers(session.buildPlayersMessage());
        DatabaseManager.logEvent(roomId, "JOIN_ROOM", usernames.get(conn) + " as " + role);
    }

    // room אנונימי, לא ניתן-לגילוי דרך JOIN_ROOM - נוצר על ידי MatchmakingService בלבד
    public void createMatchmakingRoom(WebSocket a, WebSocket b) {
        leaveCurrentRoom(a);
        leaveCurrentRoom(b);

        GameSession session = newSession(generateRoomId(), true);

        boolean aIsWhite = random.nextBoolean();
        PlayerRole roleA = aIsWhite ? PlayerRole.WHITE : PlayerRole.BLACK;
        PlayerRole roleB = aIsWhite ? PlayerRole.BLACK : PlayerRole.WHITE;

        joinRoom(a, session);
        joinRoom(b, session);
        session.getPlayerRegistry().setName(a, usernames.get(a));
        session.getPlayerRegistry().setName(b, usernames.get(b));
        session.getPlayerRegistry().setRole(a, roleA);
        session.getPlayerRegistry().setRole(b, roleB);

        a.send("ROLE " + roleA);
        b.send("ROLE " + roleB);
        String boardState = "BOARD_STATE\n" + session.serializeBoard();
        a.send(boardState);
        b.send(boardState);
        session.broadcastToMembers(session.buildPlayersMessage());
        DatabaseManager.logEvent(session.getRoomId(), "MATCH_CREATED", usernames.get(a) + " vs " + usernames.get(b));
    }

    // מחפש בכל ה-session-ים הפעילים מי מחזיק pending disconnect לשם הזה (לא רק ב-defaultSession!)
    public PlayerRole tryReconnectAnywhere(String username, WebSocket newConn) {
        for (GameSession session : allSessions) {
            PlayerRole role = session.getReconnectManager().tryReconnect(username, newConn);
            if (role != null) {
                joinRoom(newConn, session);
                newConn.send("BOARD_STATE\n" + session.serializeBoard());
                session.broadcastToMembers(session.buildPlayersMessage());
                return role;
            }
        }
        return null;
    }

    public void leaveCurrentRoom(WebSocket conn) {
        GameSession current = connectionRoom.get(conn);
        if (current != null && current != defaultSession) {
            current.removeMember(conn);
            current.getPlayerRegistry().removeRole(conn);
            cleanupIfEmpty(current);
        }
    }

    public void handleDisconnect(WebSocket conn) {
        GameSession session = connectionRoom.get(conn);
        if (session != null) {
            session.onDisconnect(conn);
            cleanupIfEmpty(session);
        }
        usernames.remove(conn);
    }

    private GameSession newSession(String roomId, boolean autoRequeueOnGameOver) {
        GameSession session = new GameSession(roomId, autoRequeueOnGameOver, scheduler);
        allSessions.add(session);
        session.startMatch();
        session.startGameLoop();
        return session;
    }

    private void joinRoom(WebSocket conn, GameSession session) {
        connectionRoom.put(conn, session);
        session.addMember(conn);
    }

    private void cleanupIfEmpty(GameSession session) {
        if (session != defaultSession && session.isEmpty()) {
            rooms.remove(session.getRoomId());
            allSessions.remove(session);
            System.out.println("[SERVER] room " + session.getRoomId() + " closed (empty)");
        }
    }

    private String generateRoomId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(ID_CHARS.charAt(random.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }
}
