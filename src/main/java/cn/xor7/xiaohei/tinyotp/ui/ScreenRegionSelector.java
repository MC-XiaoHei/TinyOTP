package cn.xor7.xiaohei.tinyotp.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.*;

public final class ScreenRegionSelector {

    private final Consumer<BufferedImage> onRegionSelected;
    private final Runnable onCancelled;
    private final BufferedImage fullScreenshot;
    private final JFrame overlay;

    private Point startPoint;
    private Rectangle selection;
    private volatile boolean done;

    public ScreenRegionSelector(
        Consumer<BufferedImage> onRegionSelected,
        Runnable onCancelled
    ) {
        this.onRegionSelected = onRegionSelected;
        this.onCancelled = onCancelled;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.fullScreenshot = captureFullScreen();

        overlay = new JFrame();
        overlay.setUndecorated(true);
        overlay.setAlwaysOnTop(true);
        overlay.setSize(screenSize);
        overlay.setLocation(0, 0);
        overlay.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(
                    fullScreenshot,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    null
                );

                g2.setColor(new Color(0, 0, 0, 160));
                int pw = getWidth(),
                    ph = getHeight();

                if (
                    selection != null &&
                    selection.width > 0 &&
                    selection.height > 0
                ) {
                    int x = selection.x,
                        y = selection.y;
                    int w = selection.width,
                        h = selection.height;
                    g2.fillRect(0, 0, pw, y);
                    g2.fillRect(0, y + h, pw, ph - y - h);
                    g2.fillRect(0, y, x, h);
                    g2.fillRect(x + w, y, pw - x - w, h);
                    g2.setColor(new Color(100, 150, 255, 230));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(x, y, w, h);
                } else {
                    g2.fillRect(0, 0, pw, ph);
                }
            }
        };

        panel.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    selection = new Rectangle(startPoint);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (
                        selection == null ||
                        selection.width < 10 ||
                        selection.height < 10
                    ) {
                        selection = null;
                        panel.repaint();
                        return;
                    }
                    done = true;
                    overlay.dispose();
                    SwingUtilities.invokeLater(() ->
                        onRegionSelected.accept(cropSelection(selection))
                    );
                }
            }
        );

        panel.addMouseMotionListener(
            new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int w = Math.abs(e.getX() - startPoint.x);
                    int h = Math.abs(e.getY() - startPoint.y);
                    if (selection == null) selection = new Rectangle();
                    selection.setBounds(x, y, w, h);
                    panel.repaint();
                }
            }
        );

        panel.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !done) {
                        done = true;
                        overlay.dispose();
                        SwingUtilities.invokeLater(onCancelled);
                    }
                }
            }
        );

        panel.setPreferredSize(screenSize);
        overlay.add(panel);
        overlay.pack();
    }

    private BufferedImage captureFullScreen() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return new Robot().createScreenCapture(new Rectangle(screenSize));
        } catch (Exception e) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }

    private BufferedImage cropSelection(Rectangle sel) {
        double sx = (double) fullScreenshot.getWidth() / overlay.getWidth();
        double sy = (double) fullScreenshot.getHeight() / overlay.getHeight();

        int ix = clamp(
            (int) Math.round(sel.x * sx),
            0,
            fullScreenshot.getWidth() - 1
        );
        int iy = clamp(
            (int) Math.round(sel.y * sy),
            0,
            fullScreenshot.getHeight() - 1
        );
        int iw = clamp(
            (int) Math.round(sel.width * sx),
            1,
            fullScreenshot.getWidth() - ix
        );
        int ih = clamp(
            (int) Math.round(sel.height * sy),
            1,
            fullScreenshot.getHeight() - iy
        );

        BufferedImage cropped = fullScreenshot.getSubimage(ix, iy, iw, ih);
        BufferedImage copy = new BufferedImage(
            cropped.getWidth(),
            cropped.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        copy.getGraphics().drawImage(cropped, 0, 0, null);
        return copy;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public void show() {
        overlay.setVisible(true);
    }
}
