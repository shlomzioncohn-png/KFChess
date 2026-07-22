package server;

import org.java_websocket.WebSocket;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MatchmakingService {

    private static final int RATING_RANGE = 100;
    private static final int MATCH_TIMEOUT_SECONDS = 60;

    private final RoomManager roomManager;
    private final ScheduledExecutorService scheduler;
    private final LinkedList<WebSocket> waitingQueue = new LinkedList<>();

    public MatchmakingService(RoomManager roomManager, ScheduledExecutorService scheduler) {
        this.roomManager = roomManager;
        this.scheduler = scheduler;
    }

    public void handlePlayRequest(WebSocket conn) {
        String username = roomManager.getUsername(conn);
        if (username == null) {
            conn.send("PLAY_FAILED not logged in");
            return;
        }

        synchronized (waitingQueue) {
            if (roomManager.hasActiveRole(conn) || waitingQueue.contains(conn)) {
                return;
            }

            int myRating = DatabaseManager.getRating(username);

            WebSocket opponent = null;
            for (WebSocket candidate : waitingQueue) {
                String candidateName = roomManager.getUsername(candidate);
                int candidateRating = DatabaseManager.getRating(candidateName);
                if (Math.abs(candidateRating - myRating) <= RATING_RANGE) {
                    opponent = candidate;
                    break;
                }
            }

            if (opponent == null) {
                waitingQueue.add(conn);
                System.out.println("[MATCHMAKING] " + username + " (rating " + myRating + ") joined queue");
                scheduler.schedule(() -> handleMatchTimeout(conn), MATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                return;
            }

            waitingQueue.remove(opponent);
            roomManager.createMatchmakingRoom(conn, opponent);

            System.out.println("[MATCHMAKING] matched " + username + " vs " + roomManager.getUsername(opponent));
        }
    }

    public void removeFromQueue(WebSocket conn) {
        synchronized (waitingQueue) {
            waitingQueue.remove(conn);
        }
    }

    private void handleMatchTimeout(WebSocket conn) {
        boolean stillWaiting;
        synchronized (waitingQueue) {
            stillWaiting = waitingQueue.remove(conn);
        }
        if (stillWaiting) {
            conn.send("NO_MATCH");
            System.out.println("[MATCHMAKING] timed out for " + roomManager.getUsername(conn));
        }
    }
}
