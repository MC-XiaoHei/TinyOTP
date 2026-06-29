package cn.xor7.xiaohei.tinyotp.crypto;

import java.security.SecureRandom;

public final class SecureRandomGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureRandomGenerator() {}

    public static byte[] generateBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static byte[] generateSalt() {
        return generateBytes(16);
    }

    public static byte[] generateIv() {
        return generateBytes(12);
    }

    public static byte[] generateSessionKey() {
        return generateBytes(32);
    }
}
