package cn.xor7.xiaohei.tinyotp.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MasterKeyDeriverTest {

    @Test
    void should_produce_32_byte_key() {
        byte[] key = MasterKeyDeriver.deriveKey("password".toCharArray(), "saltsaltsalt".getBytes());
        assertEquals(32, key.length);
    }

    @Test
    void should_be_deterministic_with_same_inputs() {
        char[] password = "test-password".toCharArray();
        byte[] salt = "test-salt-1234".getBytes();
        byte[] key1 = MasterKeyDeriver.deriveKey(password, salt);
        byte[] key2 = MasterKeyDeriver.deriveKey(password, salt);
        assertArrayEquals(key1, key2);
    }

    @Test
    void should_produce_different_key_for_different_password() {
        byte[] salt = "same-salt-1234".getBytes();
        byte[] key1 = MasterKeyDeriver.deriveKey("password-A".toCharArray(), salt);
        byte[] key2 = MasterKeyDeriver.deriveKey("password-B".toCharArray(), salt);
        assertFalse(java.util.Arrays.equals(key1, key2));
    }

    @Test
    void should_produce_different_key_for_different_salt() {
        char[] password = "same-password".toCharArray();
        byte[] key1 = MasterKeyDeriver.deriveKey(password, "salt-A".getBytes());
        byte[] key2 = MasterKeyDeriver.deriveKey(password, "salt-B".getBytes());
        assertFalse(java.util.Arrays.equals(key1, key2));
    }

    @Test
    void should_handle_empty_password() {
        byte[] key = MasterKeyDeriver.deriveKey(new char[0], "salt".getBytes());
        assertEquals(32, key.length);
    }

    @Test
    void should_handle_minimal_salt() {
        byte[] key = MasterKeyDeriver.deriveKey("password".toCharArray(), new byte[1]);
        assertEquals(32, key.length);
    }

    @Test
    void should_handle_long_password_and_salt() {
        char[] longPwd = new char[1024];
        byte[] longSalt = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            longPwd[i] = (char) (i % 128);
            longSalt[i] = (byte) i;
        }
        byte[] key = MasterKeyDeriver.deriveKey(longPwd, longSalt);
        assertEquals(32, key.length);
    }

    @Test
    void should_not_produce_all_zeros() {
        byte[] key = MasterKeyDeriver.deriveKey("non-zero".toCharArray(), "non-zero-salt".getBytes());
        boolean allZero = true;
        for (byte b : key) {
            if (b != 0) { allZero = false; break; }
        }
        assertFalse(allZero, "derived key should not be all zeros");
    }
}
