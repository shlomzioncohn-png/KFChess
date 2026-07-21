package server;

import org.java_websocket.WebSocket;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MatchmakingService {

    private static final int RATING_RANGE = 100;
    private static final int MATCH_TIMEOUT_SECONDS = 60;

    private final PlayerRegistry playerRegistry;
    private final ScheduledExecutorService scheduler;
    private final LinkedList<WebSocket> waitingQueue = new LinkedList<>();
    private final Random random = new Random();

    public MatchmakingService(PlayerRegistry playerRegistry, ScheduledExecutorService scheduler) {
        this.playerRegistry = playerRegistry;
        this.scheduler = scheduler;
    }

    public void handlePlayRequest(WebSocket conn) {
        String username = playerRegistry.getName(conn);
        if (username == null) {
            conn.send("PLAY_FAILED not logged in");
            return;
        }

        synchronized (waitingQueue) {
            if (playerRegistry.hasRole(conn) || waitingQueue.contains(conn)) {
                return;
            }

            int myRating = DatabaseManager.getRating(username);

            WebSocket opponent = null;
            for (WebSocket candidate : waitingQueue) {
                String candidateName = playerRegistry.getName(candidate);
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

            boolean connIsWhite = random.nextBoolean();
            PlayerRole connRole = connIsWhite ? PlayerRole.WHITE : PlayerRole.BLACK;
            PlayerRole opponentRole = connIsWhite ? PlayerRole.BLACK : PlayerRole.WHITE;

            playerRegistry.setRole(conn, connRole);
            playerRegistry.setRole(opponent, opponentRole);

            conn.send("ROLE " + connRole);
            opponent.send("ROLE " + opponentRole);

            System.out.println("[MATCHMAKING] matched " + username + " (" + connRole + ") vs "
                    + playerRegistry.getName(opponent) + " (" + opponentRole + ")");
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
            System.out.println("[MATCHMAKING] timed out for " + playerRegistry.getName(conn));
        }
    }
}
