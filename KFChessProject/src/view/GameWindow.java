package view;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindow {

    private JFrame frame;
    private JLabel imageLabel;
    private volatile ClickListener clickListener;
    private volatile ClickListener rightClickListener;

    public void open(Img initialImage) {
        SwingUtilities.invokeLater(() -> {
            imageLabel = new JLabel(new ImageIcon(initialImage.get()));
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        if (rightClickListener != null) {
                            rightClickListener.onClick(e.getX(), e.getY());
                        }
                    } else {
                        if (clickListener != null) {
                            clickListener.onClick(e.getX(), e.getY());
                        }
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

    public void setRightClickListener(ClickListener listener) {
        this.rightClickListener = listener;
    }
}