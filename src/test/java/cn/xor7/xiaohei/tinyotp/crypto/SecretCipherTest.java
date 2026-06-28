package cn.xor7.xiaohei.tinyotp.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecretCipherTest {

    @Test
    void should_encrypt_and_decrypt() throws Exception {
        byte[] plaintext = "Hello TinyOTP!".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(plaintext, key);
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_encrypt_with_iv() throws Exception {
        byte[] plaintext = "Test message".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        byte[] iv = SecureRandomGenerator.generateIv();
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(plaintext, key, iv);
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_throw_on_wrong_key() {
        byte[] plaintext = "Secret".getBytes();
        byte[] key1 = SecureRandomGenerator.generateBytes(32);
        byte[] key2 = SecureRandomGenerator.generateBytes(32);
        assertThrows(Exception.class, () -> {
            SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(plaintext, key1);
            SecretCipher.decrypt(encrypted, key2);
        });
    }

    @Test
    void should_handle_empty_plaintext() throws Exception {
        byte[] plaintext = new byte[0];
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(plaintext, key);
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_produce_different_ciphertexts() throws Exception {
        byte[] plaintext = "Same".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData e1 = SecretCipher.encrypt(plaintext, key);
        SecretCipher.EncryptedData e2 = SecretCipher.encrypt(plaintext, key);
        assertFalse(java.util.Arrays.equals(e1.getCiphertextWithTag(), e2.getCiphertextWithTag()));
    }
}
