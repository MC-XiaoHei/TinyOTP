package cn.xor7.xiaohei.tinyotp.ui;

import cn.xor7.xiaohei.tinyotp.model.TotpEntry;
import cn.xor7.xiaohei.tinyotp.service.VaultService;
import java.awt.*;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AddEntryDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(
        AddEntryDialog.class
    );

    private final VaultService vault;
    private final TotpEntry editing;
    private JTextField issuerField, accountField, secretField;

    public AddEntryDialog(JFrame owner, VaultService vault, TotpEntry editing) {
        super(owner, editing == null ? "添加条目" : "编辑条目", true);
        this.vault = vault;
        this.editing = editing;

        setLayout(new GridBagLayout());
        setResizable(false);
        setPreferredSize(new Dimension(400, 280));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 20, 4, 20);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        JLabel title = new JLabel(editing == null ? "添加条目" : "编辑条目");
        title.setFont(title.getFont().deriveFont(18f));
        c.gridy = 0;
        c.insets = new Insets(12, 20, 8, 20);
        add(title, c);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(3, 0, 3, 0);
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.gridx = 0;
        fc.weightx = 1.0;

        issuerField = new JTextField(20);
        issuerField.setToolTipText("例如: GitHub");
        accountField = new JTextField(20);
        accountField.setToolTipText("例如: user@example.com");
        secretField = new JTextField(20);
        secretField.setToolTipText("Base32 密钥");

        fc.gridy = 0;
        form.add(new JLabel("颁发者"), fc);
        fc.gridy = 1;
        form.add(issuerField, fc);
        fc.gridy = 2;
        form.add(new JLabel("账户"), fc);
        fc.gridy = 3;
        form.add(accountField, fc);
        fc.gridy = 4;
        form.add(new JLabel("密钥"), fc);
        fc.gridy = 5;
        form.add(secretField, fc);

        if (editing != null) {
            issuerField.setText(editing.getIssuer());
            accountField.setText(editing.getAccount());
            secretField.setText(editing.getSecret());
        }

        c.gridy = 1;
        c.insets = new Insets(4, 20, 4, 20);
        add(form, c);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("取消");
        JButton saveBtn = new JButton("保存");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        c.gridy = 2;
        c.insets = new Insets(8, 20, 12, 20);
        add(btnPanel, c);

        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        String issuer = issuerField.getText();
        String account = accountField.getText();
        String secret = secretField.getText();
        if (issuer == null || issuer.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "颁发者不能为空");
            return;
        }
        if (secret == null || secret.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "密钥不能为空");
            return;
        }

        try {
            if (editing != null) {
                editing.setIssuer(issuer.trim());
                editing.setAccount(account != null ? account.trim() : "");
                editing.setSecret(secret.trim());
                vault.updateEntry(editing);
            } else {
                TotpEntry e = new TotpEntry();
                e.setIssuer(issuer.trim());
                e.setAccount(account != null ? account.trim() : "");
                e.setSecret(secret.trim());
                vault.addEntry(e);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage());
        }
    }
}
