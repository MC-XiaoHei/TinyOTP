package cn.xor7.xiaohei.tinyotp;

import cn.xor7.xiaohei.tinyotp.core.OtpAuthParser;
import cn.xor7.xiaohei.tinyotp.model.TotpEntry;
import cn.xor7.xiaohei.tinyotp.platform.PlatformProvider;
import cn.xor7.xiaohei.tinyotp.service.VaultService;
import cn.xor7.xiaohei.tinyotp.ui.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.nio.file.Path;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TinyOtpApp {

    private static final Logger log = LoggerFactory.getLogger(TinyOtpApp.class);

    private static TinyOtpApp instance;
    private JFrame frame;
    private VaultService vault;
    private MainPanel mainPanel;

    public static TinyOtpApp getInstance() {
        return instance;
    }

    public JFrame getFrame() {
        return frame;
    }

    public TinyOtpApp() {
        instance = this;
        Path vaultPath = Path.of(
            System.getProperty("user.home"),
            ".tinyotp",
            "vault.dat"
        );
        vault = new VaultService(vaultPath);

        frame = new JFrame("TinyOTP");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setResizable(false);

        if (vault.isFirstTime()) {
            showSetupView();
        } else {
            showUnlockView();
        }
    }

    private void show(JPanel panel) {
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // cache native HWND right after window appears (needed by JHello)
        PlatformProvider.cacheHwnd(frame);
    }

    public void showSetupView() {
        SetupPanel p = new SetupPanel(vault, this::showMainView);
        show(p);
    }

    public void showUnlockView() {
        UnlockPanel p = new UnlockPanel(vault, this::showMainView);
        show(p);
    }

    public void showMainView() {
        log.info("showMainView");
        frame.setResizable(true);
        frame.setMinimumSize(new java.awt.Dimension(420, 320));

        mainPanel = new MainPanel(vault);
        mainPanel.init(this::showAddEntryDialog, this::startScanQr);
        show(mainPanel);
        mainPanel.refreshList();
        mainPanel.startTimer();

        // tray icon after unlock
        new TrayIconManager(this::showWindow, this::exitApp).addToTray();
    }

    // ── Dialogs ──

    public void showAddEntryDialog() {
        showAddEntryDialog(null);
    }

    public void showAddEntryDialog(TotpEntry entry) {
        AddEntryDialog dlg = new AddEntryDialog(frame, vault, entry);
        dlg.setVisible(true);
        if (mainPanel != null) mainPanel.refreshList();
    }

    // ── QR Scan ──

    public void startScanQr() {
        frame.setVisible(false);
        // small delay so the window actually disappears before screenshot
        javax.swing.Timer timer = new javax.swing.Timer(300, ev -> {
            new ScreenRegionSelector(this::handleScanResult, () ->
                frame.setVisible(true)
            ).show();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void handleScanResult(java.awt.image.BufferedImage image) {
        try {
            var clean = new java.awt.image.BufferedImage(
                image.getWidth(),
                image.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_RGB
            );
            clean.getGraphics().drawImage(image, 0, 0, null);

            var src =
                new com.google.zxing.client.j2se.BufferedImageLuminanceSource(
                    clean
                );
            var bitmap = new com.google.zxing.BinaryBitmap(
                new com.google.zxing.common.HybridBinarizer(src)
            );
            var hints = new java.util.HashMap<
                com.google.zxing.DecodeHintType,
                Object
            >();
            hints.put(com.google.zxing.DecodeHintType.TRY_HARDER, true);
            var zx = new com.google.zxing.MultiFormatReader().decode(
                bitmap,
                hints
            );
            var otp = OtpAuthParser.parse(zx.getText());

            TotpEntry e = new TotpEntry();
            e.setIssuer(otp.getIssuer());
            e.setAccount(otp.getAccount());
            e.setSecret(otp.getSecret());
            vault.addEntry(e);

            JOptionPane.showMessageDialog(frame, "已添加 " + otp.getIssuer());
        } catch (com.google.zxing.NotFoundException ex) {
            JOptionPane.showMessageDialog(frame, "未识别到二维码");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                frame,
                "二维码格式错误: " + ex.getMessage()
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "识别失败: " + ex.getMessage()
            );
        }
        frame.setVisible(true);
        if (mainPanel != null) mainPanel.refreshList();
    }

    // ── Tray ──

    private void showWindow() {
        SwingUtilities.invokeLater(() -> {
            if (frame.isVisible()) {
                frame.toFront();
            } else if (vault.isUnlocked()) {
                frame.setVisible(true);
            } else if (vault.isFirstTime()) {
                showSetupView();
            } else {
                showUnlockView();
            }
        });
    }

    private void exitApp() {
        if (mainPanel != null) mainPanel.stopTimer();
        System.exit(0);
    }

    // ── Entry point ──

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new TinyOtpApp());
    }
}
