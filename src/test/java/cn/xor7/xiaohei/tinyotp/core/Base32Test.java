package cn.xor7.xiaohei.tinyotp.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base32Test {

    @Test
    void should_decode_hello() {
        assertArrayEquals("Hello!".getBytes(), Base32.decode("JBSWY3DPEE"));
    }

    @Test
    void should_encode_hello() {
        assertEquals("JBSWY3DPEE======", Base32.encode("Hello!".getBytes()));
    }

    @Test
    void should_roundtrip() {
        String original = "The quick brown fox jumps over the lazy dog";
        assertEquals(original, new String(Base32.decode(Base32.encode(original.getBytes()))));
    }

    @Test
    void should_validate_base32() {
        assertTrue(Base32.isValidBase32("JBSWY3DPEHPK3PXP"));
        assertFalse(Base32.isValidBase32("JBSWY3D!EHPK3PXP"));
        assertFalse(Base32.isValidBase32(""));
    }

    @Test
    void should_decode_with_padding() {
        byte[] result = Base32.decode("MFZWQ===");
        assertNotNull(result);
    }

    @Test
    void should_return_null_for_null_input() {
        assertNull(Base32.decode(null));
    }
}
