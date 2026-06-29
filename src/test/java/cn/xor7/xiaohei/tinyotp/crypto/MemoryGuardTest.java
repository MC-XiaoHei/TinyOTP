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

    @Test
    void secureEquals_should_return_true_for_equal_arrays() {
        assertTrue(
            MemoryGuard.secureEquals(
                new byte[] { 1, 2, 3 },
                new byte[] { 1, 2, 3 }
            )
        );
    }

    @Test
    void secureEquals_should_return_false_for_different_arrays() {
        assertFalse(
            MemoryGuard.secureEquals(
                new byte[] { 1, 2, 3 },
                new byte[] { 1, 2, 4 }
            )
        );
    }

    @Test
    void secureEquals_should_return_false_for_different_length() {
        assertFalse(
            MemoryGuard.secureEquals(
                new byte[] { 1, 2, 3 },
                new byte[] { 1, 2 }
            )
        );
    }

    @Test
    void secureEquals_should_return_true_for_same_reference() {
        byte[] data = { 1, 2, 3 };
        assertTrue(MemoryGuard.secureEquals(data, data));
    }

    @Test
    void secureEquals_should_return_false_when_first_is_null() {
        assertFalse(MemoryGuard.secureEquals(null, new byte[] { 1 }));
    }

    @Test
    void secureEquals_should_return_false_when_second_is_null() {
        assertFalse(MemoryGuard.secureEquals(new byte[] { 1 }, null));
    }

    @Test
    void secureEquals_should_return_true_when_both_null() {
        assertTrue(MemoryGuard.secureEquals(null, null));
    }

    @Test
    void secureEquals_should_not_short_circuit_on_different_length() {
        assertFalse(
            MemoryGuard.secureEquals(
                new byte[] { 1, 2, 3 },
                new byte[] { 1, 2, 3, 4 }
            )
        );
    }

    @Test
    void eraseThenNull_should_zero_all_elements() {
        byte[] data = { 10, 20, 30 };
        MemoryGuard.eraseThenNull(data);
        for (byte b : data) {
            assertEquals((byte) 0, b);
        }
    }

    @Test
    void eraseThenNull_should_handle_null() {
        MemoryGuard.eraseThenNull(null);
    }

    @Test
    void eraseThenNull_should_handle_empty_array() {
        MemoryGuard.eraseThenNull(new byte[0]);
    }
}
