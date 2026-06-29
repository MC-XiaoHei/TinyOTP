package cn.xor7.xiaohei.tinyotp.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionKeyManager {

    private static final Logger log = LoggerFactory.getLogger(
        SessionKeyManager.class
    );

    private byte[] sessionKey;
    private boolean active;

    public void initialize() {
        if (active) {
            log.warn("re-initializing active session key");
            destroy();
        }
        this.sessionKey = SecureRandomGenerator.generateSessionKey();
        this.active = true;
        log.debug("session key initialized");
    }

    public boolean isActive() {
        return active;
    }

    public byte[] protect(byte[] rawKey) {
        if (!active) {
            throw new IllegalStateException("Session key is not initialized");
        }
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            rawKey,
            sessionKey
        );
        byte[] combined = java.util.Arrays.copyOf(
            encrypted.getIv(),
            encrypted.getIv().length + encrypted.getCiphertextWithTag().length
        );
        System.arraycopy(
            encrypted.getCiphertextWithTag(),
            0,
            combined,
            encrypted.getIv().length,
            encrypted.getCiphertextWithTag().length
        );
        return combined;
    }

    public byte[] expose(byte[] secretBlob) {
        if (!active) {
            throw new IllegalStateException("Session key is not initialized");
        }
        byte[] iv = java.util.Arrays.copyOfRange(secretBlob, 0, 12);
        byte[] ciphertextWithTag = java.util.Arrays.copyOfRange(
            secretBlob,
            12,
            secretBlob.length
        );
        SecretCipher.EncryptedData encrypted = new SecretCipher.EncryptedData(
            iv,
            ciphertextWithTag
        );
        return SecretCipher.decrypt(encrypted, sessionKey);
    }

    public void destroy() {
        if (sessionKey != null) {
            MemoryGuard.erase(sessionKey);
            sessionKey = null;
        }
        active = false;
        log.debug("session key destroyed");
    }
}
