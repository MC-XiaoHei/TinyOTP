package cn.xor7.xiaohei.tinyotp.model;

import java.util.ArrayList;
import java.util.List;

public final class VaultData {
    private List<TotpEntry> entries = new ArrayList<>();
    private VaultConfig config = new VaultConfig();

    public VaultData() {}

    public List<TotpEntry> getEntries() { return entries; }
    public void setEntries(List<TotpEntry> entries) { this.entries = entries; }
    public VaultConfig getConfig() { return config; }
    public void setConfig(VaultConfig config) { this.config = config; }
}
