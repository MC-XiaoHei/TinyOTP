package cn.xor7.xiaohei.tinyotp.crypto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MemoryGuardTest {

    @Test
    void erase_byte_should_zero_all_elements() {
        byte[] data = { 1, 2, 3, 4, 5 };
        MemoryGuard.erase(data);
        for (byte b : data) {
            assertEquals((byte) 0, b);
        }
    }

    @Test
    void erase_byte_should_handle_empty_array() {
        byte[] data = new byte[0];
        MemoryGuard.erase(data);
        assertEquals(0, data.length);
    }

    @Test
    void erase_byte_should_handle_null() {
        MemoryGuard.erase((byte[]) null);
    }

    @Test
    void erase_byte_should_handle_large_array() {
        byte[] data = new byte[10_000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i & 0xFF);
        MemoryGuard.erase(data);
        for (byte b : data) {
            assertEquals((byte) 0, b);
        }
    }

    @Test
    void erase_char_should_zero_all_elements() {
        char[] data = { 'a', 'b', 'c', 'd', 'e' };
        MemoryGuard.erase(data);
        for (char c : data) {
            assertEquals('\0', c);
        }
    }

    @Test
    void erase_char_should_handle_empty_array() {
        char[] data = new char[0];
        MemoryGuard.erase(data);
        assertEquals(0, data.length);
    }

    @Test
    void erase_char_should_handle_null() {
        MemoryGuard.erase((char[]) null);
    }
}
