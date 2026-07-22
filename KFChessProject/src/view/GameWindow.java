package view;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindow {

    private JFrame frame;
    private JLabel imageLabel;
    private volatile ClickListener clickListener;
    private volatile ClickListener rightClickListener;
    private volatile Runnable resizeListener;

    public void open(Img initialImage, Dimension minimumSize) {
        SwingUtilities.invokeLater(() -> {
            imageLabel = new JLabel(new ImageIcon(initialImage.get()));
            // חובה: אחרת JLabel ממרכז את האייקון כשיש פער בין גודל הקנבס (מעוגל למטה, cellSize*8)
            // לגודל הזמין בפועל - וקליקים (יחסית ל-label) לא היו תואמים לאייקון המצויר בפועל.
            imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
            imageLabel.setVerticalAlignment(SwingConstants.TOP);
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
            imageLabel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    Runnable listener = resizeListener;
                    if (listener != null) {
                        listener.run();
                    }
                }
            });

            frame = new JFrame("KungFu Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.setMinimumSize(minimumSize);
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

    // גודל האזור הזמין בפועל לציור - עשוי להיקרא מ-threads שונים (render loop, resize), לא רק EDT
    public Dimension getContentSize() {
        return imageLabel != null ? imageLabel.getSize() : new Dimension(0, 0);
    }

    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }

    public void setRightClickListener(ClickListener listener) {
        this.rightClickListener = listener;
    }

    public void setResizeListener(Runnable listener) {
        this.resizeListener = listener;
    }
}
