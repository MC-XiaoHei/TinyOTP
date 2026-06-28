package cn.xor7.xiaohei.tinyotp.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public final class TrayIconManager {
    private final Runnable onShow;
    private final Runnable onExit;
    private TrayIcon trayIcon;
    private boolean added;

    public TrayIconManager(Runnable onShow, Runnable onExit) {
        this.onShow = onShow;
        this.onExit = onExit;
    }

    public void addToTray() {
        if (!SystemTray.isSupported() || added) return;

        PopupMenu menu = new PopupMenu();

        MenuItem showItem = new MenuItem("\u663e\u793a TinyOTP");
        showItem.addActionListener(e -> onShow.run());

        MenuItem exitItem = new MenuItem("\u9000\u51fa");
        exitItem.addActionListener(e -> onExit.run());

        menu.add(showItem);
        menu.addSeparator();
        menu.add(exitItem);

        trayIcon = new TrayIcon(createIcon(), "TinyOTP", menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> onShow.run());

        try {
            SystemTray.getSystemTray().add(trayIcon);
            added = true;
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon: " + e.getMessage());
        }
    }

    public void removeFromTray() {
        if (added && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
            added = false;
        }
    }

    private Image createIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(78, 201, 176));
            g.fillOval(0, 0, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            String text = "T";
            int x = (16 - fm.stringWidth(text)) / 2;
            int y = (16 - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, x, y);
        } finally {
            g.dispose();
        }
        return image;
    }
}
