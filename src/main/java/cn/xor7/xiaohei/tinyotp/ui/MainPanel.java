package cn.xor7.xiaohei.tinyotp.ui;

import cn.xor7.xiaohei.tinyotp.model.TotpEntry;
import cn.xor7.xiaohei.tinyotp.service.VaultService;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // Toast shown at bottom-center
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

        // ── Toolbar ──
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

        // ── Entry list ──
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

        // ── Bottom bar: countdown + toast (layered) ──
        countdownLabel.setFont(countdownLabel.getFont().deriveFont(13f));
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        toastLabel.setFont(toastLabel.getFont().deriveFont(13f));
        toastLabel.setForeground(new Color(0x33, 0x33, 0x33));
        toastLabel.setVisible(false);

        // bottom: countdown (always visible) + toast (appears on top)
        JPanel bottomPanel = new JPanel(new CardLayout());
        bottomPanel.add(countdownLabel, "countdown");
        bottomPanel.add(toastLabel, "toast");
        add(bottomPanel, BorderLayout.SOUTH);

        // ── Actions ──
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

        // 1. Update codes
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

        // 2. Countdown
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
            "确认删除 " + sel.getIssuer() + " 的条目？",
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
            try {
                vault.exportBackup(ch.getSelectedFile().toPath());
                showToast("导出成功");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "导出失败: " + e.getMessage()
                );
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
            try {
                vault.importBackup(ch.getSelectedFile().toPath());
                refreshList();
                showToast("导入成功");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "导入失败: " + e.getMessage()
                );
            }
        }
    }
}
