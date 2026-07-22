package view;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * חלון לא-מודלי לאיסוף שם משתמש/סיסמה - באותה ארכיטקטורה בדיוק כמו RoomDialog:
 * לא נוגע ברשת בכלל, רק אוסף קלט ומחזיר פעולות דרך queue. הקורא (Main.java) שולח
 * LOGIN בפועל וממתין לתשובה - ואם היא נכשלת, יכול להציג שגיאה עם showError() ולתת
 * למשתמש לנסות שוב באותו חלון, בלי לאבד את שם המשתמש שהוא כבר הקליד.
 */
public class LoginDialog {

    public enum Action { LOGIN, CANCEL }

    public record Credentials(String username, String password) {}

    private JDialog dialog;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    private final BlockingQueue<Action> actions = new LinkedBlockingQueue<>();

    public void open() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialog = new JDialog((Frame) null, "Login", false);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                usernameField = new JTextField(12);
                passwordField = new JPasswordField(12);
                statusLabel = new JLabel("Enter your username and password");

                JButton loginButton = new JButton("Login");
                JButton cancelButton = new JButton("Cancel");

                loginButton.addActionListener(e -> {
                    if (usernameField.getText().trim().isEmpty() || passwordField.getPassword().length == 0) {
                        statusLabel.setText("Please enter both username and password.");
                        return;
                    }
                    actions.add(Action.LOGIN);
                });

                cancelButton.addActionListener(e -> actions.add(Action.CANCEL));

                JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                fieldsPanel.add(new JLabel("Username:"));
                fieldsPanel.add(usernameField);
                fieldsPanel.add(new JLabel("Password:"));
                fieldsPanel.add(passwordField);

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(loginButton);
                buttonPanel.add(cancelButton);

                JPanel root = new JPanel();
                root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
                statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                root.add(statusLabel);
                root.add(fieldsPanel);
                root.add(buttonPanel);

                dialog.add(root);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            System.out.println("[UI] LoginDialog failed: " + e.getMessage());
        }
    }

    public Action awaitAction() throws InterruptedException {
        return actions.take();
    }

    public Credentials getCredentials() {
        return new Credentials(usernameField.getText().trim(), new String(passwordField.getPassword()));
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (dialog != null) {
                dialog.dispose();
            }
        });
    }
}
