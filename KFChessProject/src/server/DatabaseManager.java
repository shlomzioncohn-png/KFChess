package server;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chess.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initSchema() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    rating INTEGER NOT NULL DEFAULT 1200
                );
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            System.out.println("[DB] schema ready");
        } catch (SQLException e) {
            System.out.println("[DB] error: " + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, PasswordHasher.hash(password));
            stmt.executeUpdate();

            System.out.println("[DB] registered user: " + username);
            return true;

        } catch (SQLException e) {
            System.out.println("[DB] registration failed for " + username + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean verifyLogin(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String storedHash = rs.getString("password_hash");
            String attemptHash = PasswordHasher.hash(password);
            return storedHash.equals(attemptHash);

        } catch (SQLException e) {
            System.out.println("[DB] login check failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("[DB] userExists check failed: " + e.getMessage());
            return false;
        }
    }

    public static int getRating(String username) {
        String sql = "SELECT rating FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating");
            }
            return 1200;
        } catch (SQLException e) {
            System.out.println("[DB] getRating failed: " + e.getMessage());
            return 1200;
        }
    }

    public static void updateRating(String username, int newRating) {
        String sql = "UPDATE users SET rating = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newRating);
            stmt.setString(2, username);
            stmt.executeUpdate();
            System.out.println("[DB] updated rating for " + username + " to " + newRating);
        } catch (SQLException e) {
            System.out.println("[DB] updateRating failed: " + e.getMessage());
        }
    }
}