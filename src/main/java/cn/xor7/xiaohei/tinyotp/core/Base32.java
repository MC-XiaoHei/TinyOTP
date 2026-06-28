package cn.xor7.xiaohei.tinyotp.core;

public final class Base32 {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] DECODE_TABLE = buildDecodeTable();

    private Base32() {}

    private static int[] buildDecodeTable() {
        int[] table = new int[128];
        for (int i = 0; i < table.length; i++) {
            table[i] = -1;
        }
        for (int i = 0; i < ALPHABET.length(); i++) {
            table[ALPHABET.charAt(i)] = i;
        }
        return table;
    }

    public static byte[] decode(String base32) {
        if (base32 == null) {
            return null;
        }
        String cleaned = base32.replace(" ", "").replace("-", "").toUpperCase();
        int paddingIndex = cleaned.indexOf('=');
        if (paddingIndex != -1) {
            cleaned = cleaned.substring(0, paddingIndex);
        }
        if (cleaned.isEmpty()) {
            return new byte[0];
        }
        int bitLength = cleaned.length() * 5;
        int byteLength = bitLength / 8;
        byte[] result = new byte[byteLength];
        int buffer = 0;
        int bitsRemaining = 0;
        int pos = 0;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c >= 128 || DECODE_TABLE[c] == -1) {
                throw new IllegalArgumentException(
                    "Invalid Base32 character: " + c
                );
            }
            buffer = (buffer << 5) | DECODE_TABLE[c];
            bitsRemaining += 5;
            if (bitsRemaining >= 8) {
                bitsRemaining -= 8;
                result[pos++] = (byte) ((buffer >>> bitsRemaining) & 0xFF);
            }
        }
        return result;
    }

    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        int buffer = 0;
        int bitsRemaining = 0;
        StringBuilder result = new StringBuilder();
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsRemaining += 8;
            while (bitsRemaining >= 5) {
                bitsRemaining -= 5;
                int index = (buffer >>> bitsRemaining) & 0x1F;
                result.append(ALPHABET.charAt(index));
            }
        }
        if (bitsRemaining > 0) {
            buffer = buffer << (5 - bitsRemaining);
            int index = buffer & 0x1F;
            result.append(ALPHABET.charAt(index));
        }
        int padding;
        switch (result.length() % 8) {
            case 2:
                padding = 6;
                break;
            case 4:
                padding = 4;
                break;
            case 5:
                padding = 3;
                break;
            case 7:
                padding = 1;
                break;
            default:
                padding = 0;
        }
        for (int i = 0; i < padding; i++) {
            result.append('=');
        }
        return result.toString();
    }

    public static boolean isValidBase32(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        String cleaned = input.replace(" ", "").replace("-", "").toUpperCase();
        int paddingIndex = cleaned.indexOf('=');
        String core =
            paddingIndex != -1 ? cleaned.substring(0, paddingIndex) : cleaned;
        if (core.isEmpty()) {
            return false;
        }
        for (int i = 0; i < core.length(); i++) {
            char c = core.charAt(i);
            if (c >= 128 || DECODE_TABLE[c] == -1) {
                return false;
            }
        }
        int paddingLength = cleaned.length() - core.length();
        if (paddingLength > 6) {
            return false;
        }
        for (int i = core.length(); i < cleaned.length(); i++) {
            if (cleaned.charAt(i) != '=') {
                return false;
            }
        }
        return true;
    }
}
