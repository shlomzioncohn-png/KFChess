package view;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindow {

    private JFrame frame;
    private JLabel imageLabel;
    private ClickListener clickListener;

    public void open(Img initialImage) {
        SwingUtilities.invokeLater(() -> {
            imageLabel = new JLabel(new ImageIcon(initialImage.get()));
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (clickListener != null) {
                        clickListener.onClick(e.getX(), e.getY());
                    }
                }
            });

            frame = new JFrame("KungFu Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(imageLabel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public void update(Img newImage) {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(new ImageIcon(newImage.get()));
            imageLabel.repaint();
        });
    }

    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }
}