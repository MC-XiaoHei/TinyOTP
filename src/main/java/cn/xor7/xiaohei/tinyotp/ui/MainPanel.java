package cn.xor7.xiaohei.tinyotp.ui;

import cn.xor7.xiaohei.tinyotp.model.TotpEntry;
import cn.xor7.xiaohei.tinyotp.service.VaultService;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.time.Instant;
import javax.swing.*;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MainPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(MainPanel.class);

    private final VaultService vault;
    private final DefaultListModel<TotpEntry> listModel =
        new DefaultListModel<>();
    private final JList<TotpEntry> entryList = new JList<>(listModel);
    private final JLabel countdownLabel = new JLabel("", SwingConstants.CENTER);

    private final JLabel toastLabel = new JLabel("", SwingConstants.CENTER);
    private final Timer toastTimer = new Timer(1000, e -> {
        JPanel parent = (JPanel) toastLabel.getParent();
        CardLayout cl = (CardLayout) parent.getLayout();
        cl.show(parent, "countdown");
    });

    private Runnable onAddEntry;
    private Runnable onScanQr;
    private Timer refreshTimer;

    public MainPanel(VaultService vault) {
        this.vault = vault;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(380, 380));

        toastTimer.setRepeats(false);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton addBtn = new JButton("添加");
        JPopupMenu addMenu = new JPopupMenu();
        JMenuItem manualItem = new JMenuItem("手动添加");
        JMenuItem scanItem = new JMenuItem("扫描二维码");
        addMenu.add(manualItem);
        addMenu.add(scanItem);
        addBtn.addActionListener(e ->
            addMenu.show(addBtn, 0, addBtn.getHeight())
        );

        JButton delBtn = new JButton("删除");

        JButton backupBtn = new JButton("备份");
        JPopupMenu backupMenu = new JPopupMenu();
        JMenuItem exportItem = new JMenuItem("导出备份");
        JMenuItem importItem = new JMenuItem("导入备份");
        backupMenu.add(exportItem);
        backupMenu.add(importItem);
        backupBtn.addActionListener(e ->
            backupMenu.show(backupBtn, 0, backupBtn.getHeight())
        );

        toolbar.add(addBtn);
        toolbar.add(delBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(backupBtn);
        add(toolbar, BorderLayout.NORTH);
        entryList.setCellRenderer(new TotpEntryRenderer());
        entryList.setSelectionBackground(new Color(0xD1, 0xE0, 0xF7));
        entryList.setSelectionForeground(Color.BLACK);
        entryList.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int idx = entryList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        Rectangle cell = entryList.getCellBounds(idx, idx);
                        if (cell != null && cell.contains(e.getPoint())) {
                            TotpEntry sel = listModel.get(idx);
                            if (sel != null) {
                                String code = sel.getCurrentCode();
                                if (code != null && !code.isEmpty()) {
                                    Toolkit.getDefaultToolkit()
                                        .getSystemClipboard()
                                        .setContents(
                                            new StringSelection(code),
                                            null
                                        );
                                    showToast("已复制");
                                }
                            }
                            return;
                        }
                    }
                    entryList.clearSelection();
                }
            }
        );
        JScrollPane scrollPane = new JScrollPane(entryList);
        add(scrollPane, BorderLayout.CENTER);
        countdownLabel.setFont(countdownLabel.getFont().deriveFont(13f));
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        toastLabel.setFont(toastLabel.getFont().deriveFont(13f));
        toastLabel.setForeground(new Color(0x33, 0x33, 0x33));
        toastLabel.setVisible(false);
        JPanel bottomPanel = new JPanel(new CardLayout());
        bottomPanel.add(countdownLabel, "countdown");
        bottomPanel.add(toastLabel, "toast");
        add(bottomPanel, BorderLayout.SOUTH);
        manualItem.addActionListener(e -> {
            if (onAddEntry != null) onAddEntry.run();
        });
        scanItem.addActionListener(e -> {
            if (onScanQr != null) onScanQr.run();
        });
        delBtn.addActionListener(e -> onDelete());
        exportItem.addActionListener(e -> onExport());
        importItem.addActionListener(e -> onImport());
    }

    public void init(Runnable onAddEntry, Runnable onScanQr) {
        this.onAddEntry = onAddEntry;
        this.onScanQr = onScanQr;
    }

    public void startTimer() {
        if (refreshTimer != null) refreshTimer.stop();
        refreshTimer = new Timer(1000, e -> refreshAll());
        refreshTimer.start();
    }

    public void stopTimer() {
        if (refreshTimer != null) refreshTimer.stop();
    }

    private void refreshAll() {
        long now = Instant.now().getEpochSecond();
        for (int i = 0; i < listModel.size(); i++) {
            TotpEntry entry = listModel.get(i);
            if (entry != null) {
                try {
                    entry.setCurrentCode(vault.generateCode(entry));
                } catch (Exception ignored) {
                    entry.setCurrentCode(null);
                }
                listModel.set(i, entry);
            }
        }
        long remaining = 30 - (now % 30);
        String color;
        if (remaining > 10) color = "#4caf50";
        else if (remaining > 5) color = "#ff9800";
        else color = "#f44336";
        countdownLabel.setText(
            "<html><font color='" +
                color +
                "'>下次刷新: " +
                remaining +
                "s</font></html>"
        );
    }

    public void refreshList() {
        listModel.clear();
        for (TotpEntry e : vault.getEntries()) {
            listModel.addElement(e);
        }
    }

    private void showToast(String msg) {
        toastLabel.setText(msg);
        CardLayout cl = (CardLayout) (
            (JPanel) toastLabel.getParent()
        ).getLayout();
        cl.show(toastLabel.getParent(), "toast");
        toastTimer.restart();
    }

    private void onDelete() {
        TotpEntry sel = entryList.getSelectedValue();
        if (sel == null) return;
        int r = JOptionPane.showConfirmDialog(
            this,
            "确认删除 " + sel.displayIssuer() + " 的条目？",
            "确认",
            JOptionPane.YES_NO_OPTION
        );
        if (r == JOptionPane.YES_OPTION) {
            try {
                vault.deleteEntry(sel);
                refreshList();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "删除失败: " + e.getMessage()
                );
            }
        }
    }

    private char[] promptPassword(boolean confirm) {
        JPasswordField pw1 = new JPasswordField(20);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 4, 0);
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel(confirm ? "设置备份密码" : "输入备份密码"), c);
        c.gridy = 1;
        panel.add(pw1, c);

        JPasswordField pw2 = null;
        if (confirm) {
            pw2 = new JPasswordField(20);
            c.gridy = 2;
            panel.add(new JLabel("再次输入密码"), c);
            c.gridy = 3;
            panel.add(pw2, c);
        }

        int ret = JOptionPane.showConfirmDialog(
            this,
            panel,
            confirm ? "导出备份 - 设置密码" : "导入备份 - 输入密码",
            JOptionPane.OK_CANCEL_OPTION
        );
        if (ret != JOptionPane.OK_OPTION) return null;

        char[] p1 = pw1.getPassword();
        if (p1.length == 0) {
            JOptionPane.showMessageDialog(this, "密码不能为空");
            return null;
        }
        if (confirm) {
            char[] p2 = pw2.getPassword();
            if (!java.util.Arrays.equals(p1, p2)) {
                JOptionPane.showMessageDialog(this, "两次输入的密码不一致");
                java.util.Arrays.fill(p1, '\0');
                java.util.Arrays.fill(p2, '\0');
                return null;
            }
            java.util.Arrays.fill(p2, '\0');
        }
        return p1;
    }

    private void onExport() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("导出备份");
        ch.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter(
                "TinyOTP 备份",
                "tinyotp"
            )
        );
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            char[] password = promptPassword(true);
            if (password == null) return;
            try {
                Path selected = ch.getSelectedFile().toPath();
                if (!selected.toString().endsWith(".tinyotp")) {
                    selected = selected.resolveSibling(
                        selected.getFileName() + ".tinyotp"
                    );
                }
                vault.exportBackup(selected, password);
                showToast("导出成功");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "导出失败: " + e.getMessage()
                );
            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        }
    }

    private void onImport() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("导入备份");
        ch.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter(
                "TinyOTP 备份",
                "tinyotp"
            )
        );
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            char[] password = promptPassword(false);
            if (password == null) return;
            try {
                vault.importBackup(ch.getSelectedFile().toPath(), password);
                refreshList();
                showToast("导入成功");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "导入失败: " + e.getMessage()
                );
            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        }
    }
}
