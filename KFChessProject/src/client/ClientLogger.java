package client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * לוג מקומי דו-צדדי - בנוסף ל-System.out הקיים, לא במקומו.
 * פותח וסוגר את הקובץ בכל קריאה - פשוט, מספיק לקצב ההודעות של המשחק הזה.
 */
public class ClientLogger {

    private static final String LOG_FILE = "client_log.txt";

    public static synchronized void log(String event) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(LocalDateTime.now() + " | " + event);
        } catch (IOException e) {
            System.out.println("[CLIENT LOG] could not write: " + e.getMessage());
        }
    }
}
