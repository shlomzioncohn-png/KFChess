package view;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * חלון מודלי עם textbox + 3 כפתורים (Create/Join/Cancel). לא נוגע ברשת בכלל -
 * רק אוסף מה המשתמש בחר, ומחזיר תוצאה. שליחת CREATE_ROOM/JOIN_ROOM בפועל
 * וההמתנה לתשובה קורים אצל הקורא (Main.java), אחרי ש-open() חוזרת.
 */
public class RoomDialog {

    public enum Action { CREATE, JOIN, CANCEL }

    public record Result(Action action, String roomId) {}

    public Result open() {
        AtomicReference<Result> result = new AtomicReference<>(new Result(Action.CANCEL, null));

        try {
            SwingUtilities.invokeAndWait(() -> {
                JDialog dialog = new JDialog((Frame) null, "Room", true);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                JTextField roomIdField = new JTextField(10);
                JButton createButton = new JButton("Create");
                JButton joinButton = new JButton("Join");
                JButton cancelButton = new JButton("Cancel");

                createButton.addActionListener(e -> {
                    result.set(new Result(Action.CREATE, null));
                    dialog.dispose();
                });
                joinButton.addActionListener(e -> {
                    result.set(new Result(Action.JOIN, roomIdField.getText().trim()));
                    dialog.dispose();
                });
                cancelButton.addActionListener(e -> {
                    result.set(new Result(Action.CANCEL, null));
                    dialog.dispose();
                });

                JPanel panel = new JPanel();
                panel.add(new JLabel("Room ID:"));
                panel.add(roomIdField);
                panel.add(createButton);
                panel.add(joinButton);
                panel.add(cancelButton);

                dialog.add(panel);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            System.out.println("[UI] RoomDialog failed: " + e.getMessage());
        }

        return result.get();
    }
}
