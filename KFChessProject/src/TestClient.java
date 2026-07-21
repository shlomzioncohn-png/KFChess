import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class TestClient {

    public static void main(String[] args) throws Exception {
        WebSocketClient client = new WebSocketClient(new URI("ws://localhost:8887")) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("[CLIENT] connected!");
                send("WPe2e4");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("[CLIENT] received: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("[CLIENT] closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                System.out.println("[CLIENT] error: " + ex.getMessage());
            }
        };

        client.connect();
    }
}