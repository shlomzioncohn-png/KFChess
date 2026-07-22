package view;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * מסך הבית - נבחר בין Quick Play (matchmaking רגיל) ל-Room (create/join ספציפי).
 */
public class HomeDialog {

    public enum Choice { QUICK_PLAY, ROOM }

    public Choice open() {
        AtomicReference<Choice> result = new AtomicReference<>(Choice.QUICK_PLAY);

        try {
            SwingUtilities.invokeAndWait(() -> {
                JDialog dialog = new JDialog((Frame) null, "KungFu Chess", true);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                JButton quickPlayButton = new JButton("Quick Play");
                JButton roomButton = new JButton("Room");

                quickPlayButton.addActionListener(e -> {
                    result.set(Choice.QUICK_PLAY);
                    dialog.dispose();
                });
                roomButton.addActionListener(e -> {
                    result.set(Choice.ROOM);
                    dialog.dispose();
                });

                JPanel panel = new JPanel();
                panel.add(quickPlayButton);
                panel.add(roomButton);

                dialog.add(panel);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            System.out.println("[UI] HomeDialog failed: " + e.getMessage());
        }

        return result.get();
    }
}
