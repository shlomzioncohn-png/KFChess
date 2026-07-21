import server.GameServer;

public class ServerMain {

    private static final int PORT = 8887;

    public static void main(String[] args) {
        server.DatabaseManager.initSchema();

        GameServer server = new GameServer(PORT);
        server.start();

        System.out.println("[SERVER] listening on port " + PORT);
    }
}