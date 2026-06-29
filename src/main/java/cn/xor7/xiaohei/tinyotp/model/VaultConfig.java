package cn.xor7.xiaohei.tinyotp.model;

public final class VaultConfig {

    private byte[] encryptedMasterKey;

    public VaultConfig() {}

    public byte[] getEncryptedMasterKey() {
        return encryptedMasterKey;
    }

    public void setEncryptedMasterKey(byte[] encryptedMasterKey) {
        this.encryptedMasterKey = encryptedMasterKey;
    }
}
