package cn.xor7.xiaohei.tinyotp.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HmacSha1Test {

    @Test
    void should_sign_with_known_vector_case_1() {
        byte[] key = hex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        byte[] data = "Hi There".getBytes();
        byte[] expected = hex("b617318655057264e28bc0b6fb378c8ef146be00");
        assertArrayEquals(expected, HmacSha1.sign(key, data));
    }

    @Test
    void should_sign_with_known_vector_case_2() {
        byte[] key = "Jefe".getBytes();
        byte[] data = "what do ya want for nothing?".getBytes();
        byte[] expected = hex("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79");
        assertArrayEquals(expected, HmacSha1.sign(key, data));
    }

    @Test
    void should_sign_with_known_vector_case_3() {
        byte[] key = hex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        byte[] data = hex(
            "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
        );
        byte[] expected = hex("125d7342b9ac11cd91a39af48aa17b4f63f175d3");
        assertArrayEquals(expected, HmacSha1.sign(key, data));
    }

    @Test
    void should_sign_with_known_vector_case_4() {
        byte[] key = hex("0102030405060708090a0b0c0d0e0f10111213141516171819");
        byte[] data = hex(
            "cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd"
        );
        byte[] expected = hex("4c9007f4026250c6bc8414f9bf50c86c2d7235da");
        assertArrayEquals(expected, HmacSha1.sign(key, data));
    }

    @Test
    void should_be_deterministic() {
        byte[] result1 = HmacSha1.sign("secret".getBytes(), "hello".getBytes());
        byte[] result2 = HmacSha1.sign("secret".getBytes(), "hello".getBytes());
        assertArrayEquals(result1, result2);
    }

    @Test
    void should_handle_single_byte_key() {
        byte[] result = HmacSha1.sign(new byte[] { 0x2A }, "data".getBytes());
        assertEquals(20, result.length);
    }

    @Test
    void should_handle_empty_data() {
        byte[] result = HmacSha1.sign("key".getBytes(), new byte[0]);
        assertEquals(20, result.length);
    }

    @Test
    void should_produce_20_byte_output() {
        byte[] result = HmacSha1.sign(
            "key".getBytes(),
            "The quick brown fox jumps over the lazy dog".getBytes()
        );
        assertEquals(20, result.length);
    }

    @Test
    void should_produce_different_output_for_different_keys() {
        byte[] data = "same data".getBytes();
        byte[] r1 = HmacSha1.sign("key1".getBytes(), data);
        byte[] r2 = HmacSha1.sign("key2".getBytes(), data);
        assertFalse(java.util.Arrays.equals(r1, r2));
    }

    @Test
    void should_produce_different_output_for_different_data() {
        byte[] key = "same-key".getBytes();
        byte[] r1 = HmacSha1.sign(key, "data A".getBytes());
        byte[] r2 = HmacSha1.sign(key, "data B".getBytes());
        assertFalse(java.util.Arrays.equals(r1, r2));
    }

    @Test
    void should_handle_long_key() {
        byte[] key = new byte[256];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;
        byte[] result = HmacSha1.sign(key, "data".getBytes());
        assertEquals(20, result.length);
    }

    private static byte[] hex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
                Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
