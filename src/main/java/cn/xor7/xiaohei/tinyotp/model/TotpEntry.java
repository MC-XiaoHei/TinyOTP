package cn.xor7.xiaohei.tinyotp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;

public final class TotpEntry {

    private String issuer;
    private String account;
    private String secret;

    @JsonIgnore
    private byte[] secretBlob;

    @JsonIgnore
    private String currentCode;

    private String iconColor;
    private int sortOrder;
    private Long createdAt;
    private Long updatedAt;

    public TotpEntry() {}

    public TotpEntry(String issuer, String account, byte[] secretBlob) {
        this.issuer = issuer;
        this.account = account;
        this.secretBlob = secretBlob;
        this.iconColor = "#333333";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @JsonIgnore
    public byte[] getSecretBlob() {
        return secretBlob;
    }

    public void setSecretBlob(byte[] secretBlob) {
        this.secretBlob = secretBlob;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void clearSecret() {
        if (secretBlob != null) {
            Arrays.fill(secretBlob, (byte) 0);
        }
    }

    public String getCurrentCode() {
        return currentCode;
    }

    public void setCurrentCode(String currentCode) {
        this.currentCode = currentCode;
    }

    public String displayLabel() {
        if (account != null && !account.isEmpty()) {
            return issuer + " (" + account + ")";
        }
        return issuer;
    }
}
