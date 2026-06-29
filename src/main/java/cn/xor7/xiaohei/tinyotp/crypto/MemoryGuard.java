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

    public static boolean secureEquals(byte[] a, byte[] b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
