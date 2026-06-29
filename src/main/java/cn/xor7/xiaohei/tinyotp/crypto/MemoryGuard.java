package cn.xor7.xiaohei.tinyotp.crypto;

import java.util.Arrays;

public final class MemoryGuard {

    private MemoryGuard() {}

    public static void erase(byte[] data) {
        if (data == null) return;
        Arrays.fill(data, (byte) 0);
    }

    public static void erase(char[] data) {
        if (data == null) return;
        Arrays.fill(data, '\0');
    }
}
