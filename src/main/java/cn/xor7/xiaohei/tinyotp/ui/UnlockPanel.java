package cn.xor7.xiaohei.tinyotp.ui;

import cn.xor7.xiaohei.tinyotp.service.VaultService;
import java.awt.*;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnlockPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(UnlockPanel.class);

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

        try {
            if (!vault.verifyWithHello()) {
                btn.setEnabled(true);
                btn.setText("Windows Hello");
                JOptionPane.showMessageDialog(this, "验证失败或已取消");
                return;
            }
            btn.setText("正在解锁...");
            vault.unlockWithHello();
            if (onSuccess != null) onSuccess.run();
        } catch (Exception e) {
            log.error("unlock failed", e);
            btn.setEnabled(true);
            btn.setText("Windows Hello");
            JOptionPane.showMessageDialog(this, "验证失败: " + e.getMessage());
        }
    }
}
