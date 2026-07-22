package view;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * חלון לא-מודלי עם textbox + 3 כפתורים (Create/Join/Cancel). לא נוגע ברשת בכלל -
 * רק אוסף פעולות מהמשתמש. שליחת CREATE_ROOM/JOIN_ROOM בפועל וההמתנה לתשובה
 * קורים אצל הקורא (Main.java). החלון נשאר פתוח בין ניסיונות (למשל אחרי
 * JOIN_FAILED) - הקורא יכול להציג שגיאה עם showError() ולחכות לפעולה הבאה
 * באותו חלון בדיוק, בלי לאבד את מה שהמשתמש כבר הקליד.
 */
public class RoomDialog {

    public enum Action { CREATE, JOIN, CANCEL }

    private JDialog dialog;
    private JTextField roomIdField;
    private JLabel statusLabel;

    private final BlockingQueue<Action> actions = new LinkedBlockingQueue<>();
    private volatile String lastRoomId;

    public void open() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialog = new JDialog((Frame) null, "Room", false);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                roomIdField = new JTextField(10);
                statusLabel = new JLabel("To join: enter the room code your opponent shared. To start a new game: leave empty and click Create.");

                JButton createButton = new JButton("Create");
                JButton joinButton = new JButton("Join");
                JButton cancelButton = new JButton("Cancel");

                createButton.addActionListener(e -> actions.add(Action.CREATE));

                joinButton.addActionListener(e -> {
                    String id = roomIdField.getText().trim();
                    if (id.isEmpty()) {
                        statusLabel.setText("Please enter a room code first.");
                        return;
                    }
                    lastRoomId = id;
                    actions.add(Action.JOIN);
                });

                cancelButton.addActionListener(e -> actions.add(Action.CANCEL));

                JPanel fieldPanel = new JPanel();
                fieldPanel.add(new JLabel("Room code:"));
                fieldPanel.add(roomIdField);

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(createButton);
                buttonPanel.add(joinButton);
                buttonPanel.add(cancelButton);

                JPanel root = new JPanel();
                root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
                statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                fieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                root.add(statusLabel);
                root.add(fieldPanel);
                root.add(buttonPanel);

                dialog.add(root);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            System.out.println("[UI] RoomDialog failed: " + e.getMessage());
        }
    }

    public Action awaitAction() throws InterruptedException {
        return actions.take();
    }

    public String getRoomId() {
        return lastRoomId;
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
