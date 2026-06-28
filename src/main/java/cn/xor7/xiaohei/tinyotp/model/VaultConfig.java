package cn.xor7.xiaohei.tinyotp.model;

import java.util.Arrays;

public final class VaultConfig {
    private int timeoutMinutes = 5;
    private boolean helloEnabled = false;
    private String helloCredentialName;
    private byte[] encryptedMasterKey;
    private byte[] encryptedProtectionKey;

    public VaultConfig() {}

    public int getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    public boolean isHelloEnabled() { return helloEnabled; }
    public void setHelloEnabled(boolean helloEnabled) { this.helloEnabled = helloEnabled; }
    public String getHelloCredentialName() { return helloCredentialName; }
    public void setHelloCredentialName(String helloCredentialName) { this.helloCredentialName = helloCredentialName; }
    public byte[] getEncryptedMasterKey() { return encryptedMasterKey; }
    public void setEncryptedMasterKey(byte[] encryptedMasterKey) { this.encryptedMasterKey = encryptedMasterKey; }
    public byte[] getEncryptedProtectionKey() { return encryptedProtectionKey; }
    public void setEncryptedProtectionKey(byte[] encryptedProtectionKey) { this.encryptedProtectionKey = encryptedProtectionKey; }

    public void clearSensitiveData() {
        if (encryptedMasterKey != null) Arrays.fill(encryptedMasterKey, (byte) 0);
        if (encryptedProtectionKey != null) Arrays.fill(encryptedProtectionKey, (byte) 0);
    }
}
