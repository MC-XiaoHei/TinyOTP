package cn.xor7.xiaohei.tinyotp.ui;

import cn.xor7.xiaohei.tinyotp.platform.PlatformProvider;
import cn.xor7.xiaohei.tinyotp.service.VaultService;
import java.awt.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnlockPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(
        UnlockPanel.class
    );

    private static final long HELLO_TIMEOUT_MS = 120_000;

    private final VaultService vault;
    private final Runnable onSuccess;

    public UnlockPanel(VaultService vault, Runnable onSuccess) {
        this.vault = vault;
        this.onSuccess = onSuccess;

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(400, 280));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 30, 5, 30);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        JLabel title = new JLabel("TinyOTP", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(28f));
        c.gridy = 0;
        add(title, c);

        JLabel prompt = new JLabel("请验证身份以解锁", SwingConstants.CENTER);
        prompt.setFont(prompt.getFont().deriveFont(13f));
        prompt.setForeground(Color.DARK_GRAY);
        c.gridy = 1;
        c.insets = new Insets(10, 30, 15, 30);
        add(prompt, c);

        JButton btn = new JButton("Windows Hello");
        btn.setFont(btn.getFont().deriveFont(14f));
        btn.addActionListener(e -> onHello(btn));
        c.insets = new Insets(5, 60, 5, 60);
        c.gridy = 2;
        add(btn, c);
    }

    private void onHello(JButton btn) {
        btn.setEnabled(false);
        btn.setText("正在验证...");
        log.info("=== onHello ===");

        // 1) 确保 cachedHwnd 有效（不要调用 toFront，否则会干扰 Windows 模态对话框的 Z-order）
        PlatformProvider.refreshHwnd();

        // 2) 后台执行 JHello.verify + unlockWithHello，避免阻塞 EDT
        //    （JHello.verify 会传入父窗口 HWND，Windows 原生模态机制会自动
        //     将 Hello 对话框显示在主窗口之上）
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
            () -> {
                try {
                    log.info("starting JHello.verify on worker thread");
                    boolean verified = vault.verifyWithHello();
                    if (!verified) return false;

                    log.info("verify passed, starting unlock on worker thread");
                    vault.unlockWithHello();
                    return true;
                } catch (Exception ex) {
                    log.error("hello verify/unlock failed on worker", ex);
                    throw new RuntimeException(ex);
                }
            }
        );

        // 3) 超时保护
        future = future.orTimeout(HELLO_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        // 4) 回到 EDT 处理结果
        future.whenCompleteAsync((success, ex) -> {
            if (ex != null) {
                Throwable cause =
                    ex instanceof java.util.concurrent.ExecutionException
                        ? ex.getCause()
                        : ex;
                if (
                    cause instanceof CancellationException ||
                    cause instanceof java.util.concurrent.TimeoutException
                ) {
                    log.warn("hello verify timed out or cancelled");
                    JOptionPane.showMessageDialog(this, "验证超时，请重试");
                } else {
                    log.error("unlock failed", cause);
                    JOptionPane.showMessageDialog(
                        this,
                        "验证失败: " + cause.getMessage()
                    );
                }
                btn.setEnabled(true);
                btn.setText("Windows Hello");
                return;
            }

            if (!success) {
                btn.setEnabled(true);
                btn.setText("Windows Hello");
                JOptionPane.showMessageDialog(this, "验证失败或已取消");
                return;
            }

            btn.setText("正在解锁...");
            if (onSuccess != null) onSuccess.run();
        }, SwingUtilities::invokeLater);
    }
}
