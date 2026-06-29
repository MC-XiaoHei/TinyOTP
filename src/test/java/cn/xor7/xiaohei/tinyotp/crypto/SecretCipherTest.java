package cn.xor7.xiaohei.tinyotp.crypto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecretCipherTest {

    @Test
    void should_encrypt_and_decrypt() throws Exception {
        byte[] plaintext = "Hello TinyOTP!".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key
        );
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_encrypt_with_iv() throws Exception {
        byte[] plaintext = "Test message".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        byte[] iv = SecureRandomGenerator.generateIv();
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key,
            iv
        );
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_throw_on_wrong_key() {
        byte[] plaintext = "Secret".getBytes();
        byte[] key1 = SecureRandomGenerator.generateBytes(32);
        byte[] key2 = SecureRandomGenerator.generateBytes(32);
        assertThrows(Exception.class, () -> {
            SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
                plaintext,
                key1
            );
            SecretCipher.decrypt(encrypted, key2);
        });
    }

    @Test
    void should_throw_on_tampered_ciphertext() {
        byte[] plaintext = "tamper test".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key
        );
        byte[] tampered = encrypted.getCiphertextWithTag().clone();
        tampered[0] ^= 0xFF;
        SecretCipher.EncryptedData corrupted = new SecretCipher.EncryptedData(
            encrypted.getIv(),
            tampered
        );
        assertThrows(Exception.class, () ->
            SecretCipher.decrypt(corrupted, key)
        );
    }

    @Test
    void should_throw_on_tampered_iv() {
        byte[] plaintext = "tamper iv test".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key
        );
        byte[] tamperedIv = encrypted.getIv().clone();
        tamperedIv[0] ^= 0xFF;
        SecretCipher.EncryptedData corrupted = new SecretCipher.EncryptedData(
            tamperedIv,
            encrypted.getCiphertextWithTag()
        );
        assertThrows(Exception.class, () ->
            SecretCipher.decrypt(corrupted, key)
        );
    }

    @Test
    void should_throw_on_empty_key() {
        assertThrows(Exception.class, () ->
            SecretCipher.encrypt("data".getBytes(), new byte[0])
        );
    }

    @Test
    void should_handle_empty_plaintext() throws Exception {
        byte[] plaintext = new byte[0];
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key
        );
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_produce_different_ciphertexts_for_same_plaintext()
        throws Exception {
        byte[] plaintext = "Same".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData e1 = SecretCipher.encrypt(plaintext, key);
        SecretCipher.EncryptedData e2 = SecretCipher.encrypt(plaintext, key);
        assertFalse(
            java.util.Arrays.equals(
                e1.getCiphertextWithTag(),
                e2.getCiphertextWithTag()
            )
        );
    }

    @Test
    void should_produce_different_ivs_for_same_plaintext() throws Exception {
        byte[] plaintext = "Same".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData e1 = SecretCipher.encrypt(plaintext, key);
        SecretCipher.EncryptedData e2 = SecretCipher.encrypt(plaintext, key);
        assertFalse(java.util.Arrays.equals(e1.getIv(), e2.getIv()));
    }

    @Test
    void should_handle_large_plaintext() throws Exception {
        byte[] large = new byte[1_000_000];
        for (int i = 0; i < large.length; i++) large[i] = (byte) (i & 0xFF);
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(large, key);
        assertArrayEquals(large, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void should_handle_single_byte_plaintext() throws Exception {
        byte[] plaintext = new byte[] { 0x42 };
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key
        );
        assertArrayEquals(plaintext, SecretCipher.decrypt(encrypted, key));
    }

    @Test
    void ciphertext_should_be_larger_than_plaintext() throws Exception {
        byte[] plaintext = "small".getBytes();
        byte[] key = SecureRandomGenerator.generateBytes(32);
        SecretCipher.EncryptedData encrypted = SecretCipher.encrypt(
            plaintext,
            key
        );
        assertTrue(
            encrypted.getCiphertextWithTag().length > plaintext.length,
            "ciphertext should include IV and GCM tag overhead"
        );
    }
}
