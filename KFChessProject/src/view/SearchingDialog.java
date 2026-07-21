package view;

import javax.swing.*;
import java.awt.*;

public class SearchingDialog {

    private JDialog dialog;

    public void open(String message) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialog = new JDialog((Frame) null, "Please wait", false);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                JLabel label = new JLabel(message);
                label.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                dialog.add(label);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        } catch (Exception e) {
            System.out.println("[UI] could not open searching dialog: " + e.getMessage());
        }
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (dialog != null) {
                dialog.dispose();
            }
        });
    }
}
