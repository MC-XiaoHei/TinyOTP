package cn.xor7.xiaohei.tinyotp.crypto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecureRandomGeneratorTest {

    @Test
    void generateBytes_should_return_correct_length() {
        byte[] bytes = SecureRandomGenerator.generateBytes(32);
        assertEquals(32, bytes.length);
    }

    @Test
    void generateBytes_should_return_zero_length_for_zero() {
        byte[] bytes = SecureRandomGenerator.generateBytes(0);
        assertEquals(0, bytes.length);
    }

    @Test
    void generateBytes_should_return_different_values_each_call() {
        byte[] a = SecureRandomGenerator.generateBytes(16);
        byte[] b = SecureRandomGenerator.generateBytes(16);
        assertFalse(java.util.Arrays.equals(a, b));
    }

    @Test
    void generateBytes_should_not_be_all_zeros() {
        byte[] bytes = SecureRandomGenerator.generateBytes(32);
        boolean allZero = true;
        for (byte b : bytes) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        assertFalse(allZero, "random bytes should not be all zeros");
    }

    @Test
    void generateSalt_should_return_16_bytes() {
        assertEquals(16, SecureRandomGenerator.generateSalt().length);
    }

    @Test
    void generateSalt_should_be_random() {
        assertFalse(
            java.util.Arrays.equals(
                SecureRandomGenerator.generateSalt(),
                SecureRandomGenerator.generateSalt()
            )
        );
    }

    @Test
    void generateIv_should_return_12_bytes() {
        assertEquals(12, SecureRandomGenerator.generateIv().length);
    }

    @Test
    void generateIv_should_be_random() {
        assertFalse(
            java.util.Arrays.equals(
                SecureRandomGenerator.generateIv(),
                SecureRandomGenerator.generateIv()
            )
        );
    }

    @Test
    void generateSessionKey_should_return_32_bytes() {
        assertEquals(32, SecureRandomGenerator.generateSessionKey().length);
    }

    @Test
    void should_generate_large_arrays() {
        byte[] large = SecureRandomGenerator.generateBytes(65_536);
        assertEquals(65_536, large.length);
    }
}
