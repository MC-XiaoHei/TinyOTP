package cn.xor7.xiaohei.tinyotp.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public final class SecretCipher {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final byte[] AAD = "TINYOTP".getBytes();

    private SecretCipher() {}

    public static EncryptedData encrypt(byte[] plaintext, byte[] key) {
        byte[] iv = SecureRandomGenerator.generateIv();
        return encrypt(plaintext, key, iv);
    }

    public static EncryptedData encrypt(byte[] plaintext, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(AAD);
            byte[] ciphertextWithTag = cipher.doFinal(plaintext);
            return new EncryptedData(iv, ciphertextWithTag);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES-GCM encryption failed", e);
        }
    }

    public static byte[] decrypt(EncryptedData data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, data.getIv());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(AAD);
            return cipher.doFinal(data.getCiphertextWithTag());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }

    public static final class EncryptedData {
        private final byte[] iv;
        private final byte[] ciphertextWithTag;

        public EncryptedData(byte[] iv, byte[] ciphertextWithTag) {
            this.iv = iv;
            this.ciphertextWithTag = ciphertextWithTag;
        }

        public byte[] getIv() {
            return iv;
        }

        public byte[] getCiphertextWithTag() {
            return ciphertextWithTag;
        }
    }
}
