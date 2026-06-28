package cn.xor7.xiaohei.tinyotp.ui;

import cn.xor7.xiaohei.tinyotp.model.TotpEntry;
import java.awt.*;
import java.time.Instant;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class TotpEntryRenderer
    extends JPanel
    implements ListCellRenderer<TotpEntry>
{

    private final JLabel issuerLabel = new JLabel();
    private final JLabel accountLabel = new JLabel();
    private final JLabel codeLabel = new JLabel();

    public TotpEntryRenderer() {
        setLayout(new BorderLayout(8, 0));
        setBorder(new EmptyBorder(6, 10, 6, 10));

        issuerLabel.setFont(issuerLabel.getFont().deriveFont(Font.BOLD, 13f));
        accountLabel.setFont(accountLabel.getFont().deriveFont(11f));
        accountLabel.setForeground(Color.GRAY);

        JPanel left = new JPanel(new BorderLayout(0, 1));
        left.setOpaque(false);
        left.add(issuerLabel, BorderLayout.NORTH);
        left.add(accountLabel, BorderLayout.SOUTH);

        codeLabel.setFont(new Font("Consolas", Font.BOLD, 20));

        add(left, BorderLayout.CENTER);
        add(codeLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
        JList<? extends TotpEntry> list,
        TotpEntry entry,
        int index,
        boolean isSelected,
        boolean cellHasFocus
    ) {
        if (entry == null) return this;

        issuerLabel.setText(entry.getIssuer());
        accountLabel.setText(
            entry.getAccount() != null ? entry.getAccount() : ""
        );

        String code = entry.getCurrentCode();
        codeLabel.setText(code != null ? code : "");

        // Color by remaining time
        long now = Instant.now().getEpochSecond();
        long remaining = 30 - (now % 30);

        String htmlColor;
        if (remaining > 10) {
            htmlColor = "#4caf50";
        } else if (remaining > 5) {
            htmlColor = "#ff9800";
        } else {
            htmlColor = "#f44336";
        }

        codeLabel.setForeground(Color.decode(htmlColor));

        // Selection highlight
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            issuerLabel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            issuerLabel.setForeground(list.getForeground());
        }
        setOpaque(isSelected);

        return this;
    }
}
