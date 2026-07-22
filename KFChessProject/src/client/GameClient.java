package client;

import bus.events.GameOverEvent;
import engine.GameEngine;
import models.GameState;
import models.Position;
import models.enums.PieceColor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameClient extends WebSocketClient {

    private volatile GameEngine engine;
    private volatile GameState gameState;
    private final BlockingQueue<String> loginReplies = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> matchReplies = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> roomReplies = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> boardStateReplies = new LinkedBlockingQueue<>();
    private final BlockingQueue<Boolean> returnToQueueSignal = new LinkedBlockingQueue<>();
    private volatile long disconnectDeadline = -1;
    private volatile int disconnectTotalSeconds = -1;
    private volatile long returnDeadline = -1;
    private volatile int returnTotalSeconds = -1;

    public GameClient(URI serverUri) {
        super(serverUri);
    }

    public void startNewRound(GameEngine engine, GameState gameState) {
        this.engine = engine;
        this.gameState = gameState;
    }

    public String awaitLoginReply() throws InterruptedException {
        return loginReplies.take();
    }

    public String awaitMatchReply() throws InterruptedException {
        return matchReplies.take();
    }

    public String awaitRoomReply() throws InterruptedException {
        return roomReplies.take();
    }

    // מחזירה את פריסת הלוח הנוכחית (לא לוח פתיחה) שהשרת שלח בהצטרפות/reconnect
    public String awaitBoardState() throws InterruptedException {
        return boardStateReplies.take();
    }

    public void awaitReturnToQueue() throws InterruptedException {
        returnToQueueSignal.take();
    }

    public Integer getDisconnectSecondsLeft() {
        if (disconnectDeadline < 0) {
            return null;
        }
        long remainingMs = disconnectDeadline - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return 0;
        }
        return (int) Math.ceil(remainingMs / 1000.0);
    }

    public Integer getDisconnectTotalSeconds() {
        return disconnectTotalSeconds < 0 ? null : disconnectTotalSeconds;
    }

    public Integer getReturnCountdownSecondsLeft() {
        if (returnDeadline < 0) {
            return null;
        }
        long remainingMs = returnDeadline - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return 0;
        }
        return (int) Math.ceil(remainingMs / 1000.0);
    }

    public Integer getReturnCountdownTotalSeconds() {
        return returnTotalSeconds < 0 ? null : returnTotalSeconds;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("[CLIENT] connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("[CLIENT] received: " + message);

        if (message.startsWith("LOGIN_OK") || message.startsWith("LOGIN_FAILED")) {
            ClientLogger.log("LOGIN: " + message);
            loginReplies.add(message);
            return;
        }

        if (message.startsWith("ROLE ") || message.equals("NO_MATCH")) {
            ClientLogger.log("MATCH: " + message);
            matchReplies.add(message);
            return;
        }

        if (message.startsWith("ROOM_CREATED ") || message.startsWith("ROOM_JOINED ")
                || message.startsWith("JOIN_FAILED") || message.equals("ROOM_CANCELLED")) {
            ClientLogger.log("ROOM: " + message);
            roomReplies.add(message);
            return;
        }

        if (message.startsWith("BOARD_STATE\n")) {
            boardStateReplies.add(message.substring("BOARD_STATE\n".length()));
            return;
        }

        if (message.startsWith("RETURN_TO_QUEUE_COUNTDOWN ")) {
            int seconds = Integer.parseInt(message.substring("RETURN_TO_QUEUE_COUNTDOWN ".length()).trim());
            returnTotalSeconds = seconds;
            returnDeadline = System.currentTimeMillis() + seconds * 1000L;
            return;
        }

        if (message.equals("RETURN_TO_QUEUE")) {
            returnDeadline = -1;
            returnTotalSeconds = -1;
            returnToQueueSignal.add(Boolean.TRUE);
            return;
        }

        if (message.startsWith("OPPONENT_DISCONNECTED ")) {
            int seconds = Integer.parseInt(message.substring("OPPONENT_DISCONNECTED ".length()).trim());
            disconnectTotalSeconds = seconds;
            disconnectDeadline = System.currentTimeMillis() + seconds * 1000L;
            return;
        }

        if (message.equals("OPPONENT_RECONNECTED")) {
            disconnectDeadline = -1;
            disconnectTotalSeconds = -1;
            return;
        }

        if (message.startsWith("GAMEOVER ")) {
            disconnectDeadline = -1;
            disconnectTotalSeconds = -1;
            GameEngine currentEngine = engine;
            GameState currentGameState = gameState;
            if (currentEngine == null || currentGameState == null) {
                System.out.println("[CLIENT] GAMEOVER received before round was ready - ignoring");
                return;
            }
            try {
                PieceColor winner = PieceColor.valueOf(message.substring("GAMEOVER ".length()).trim());
                if (!currentGameState.isGameOver()) {
                    currentGameState.setGameOver(true);
                    currentGameState.setWinner(winner);
                    currentEngine.getBus().publish("game.over", new GameOverEvent(winner));
                    ClientLogger.log("GAME_OVER: winner=" + winner);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("[CLIENT] bad GAMEOVER message: " + message);
            }
            return;
        }

        if (message.startsWith("JUMP ")) {
            GameEngine currentEngine = engine;
            if (currentEngine == null) {
                System.out.println("[CLIENT] JUMP received before round was ready - ignoring");
                return;
            }
            try {
                Position pos = ServerMessageParser.parseJump(message);
                currentEngine.forceJump(pos);
            } catch (IllegalArgumentException e) {
                System.out.println("[CLIENT] bad jump message: " + message);
            }
            return;
        }

        GameEngine currentEngine = engine;
        if (currentEngine == null) {
            System.out.println("[CLIENT] message received before round was ready - ignoring: " + message);
            return;
        }
        try {
            Position[] positions = ServerMessageParser.parseMove(message);
            currentEngine.forceMove(positions[0], positions[1]);
            ClientLogger.log("MOVE: " + message);
        } catch (IllegalArgumentException e) {
            System.out.println("[CLIENT] not a move message: " + message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[CLIENT] disconnected: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("[CLIENT] error: " + ex.getMessage());
    }
}
