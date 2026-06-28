package cn.xor7.xiaohei.tinyotp.core;

import cn.xor7.xiaohei.tinyotp.crypto.MemoryGuard;
import java.nio.ByteBuffer;
import java.time.Instant;

public final class TotpGenerator {

    private static final int PERIOD = 30;
    private static final int DIGITS = 6;

    public String generateCode(byte[] rawKey, Instant currentTime) {
        long counter = currentTime.getEpochSecond() / PERIOD;
        byte[] counterBytes = toBigEndianBytes(counter);
        byte[] hmac = HmacSha1.sign(rawKey, counterBytes);
        MemoryGuard.erase(rawKey);
        int offset = extractDynamicOffset(hmac);
        int truncatedHash = extractTruncatedHash(hmac, offset);
        MemoryGuard.erase(hmac);
        return formatCode(truncatedHash);
    }

    private byte[] toBigEndianBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }

    private int extractDynamicOffset(byte[] hmac) {
        return hmac[hmac.length - 1] & 0x0F;
    }

    private int extractTruncatedHash(byte[] hmac, int offset) {
        return (
            ((hmac[offset] & 0x7F) << 24) |
            ((hmac[offset + 1] & 0xFF) << 16) |
            ((hmac[offset + 2] & 0xFF) << 8) |
            (hmac[offset + 3] & 0xFF)
        );
    }

    private String formatCode(int truncatedHash) {
        int code = truncatedHash % (int) Math.pow(10, DIGITS);
        return String.format("%0" + DIGITS + "d", code);
    }
}
