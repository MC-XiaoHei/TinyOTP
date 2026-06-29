package cn.xor7.xiaohei.tinyotp.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Base32Test {

    @Test
    void should_decode_f() {
        assertArrayEquals("f".getBytes(), Base32.decode("MY======"));
    }

    @Test
    void should_decode_fo() {
        assertArrayEquals("fo".getBytes(), Base32.decode("MZXQ===="));
    }

    @Test
    void should_decode_foo() {
        assertArrayEquals("foo".getBytes(), Base32.decode("MZXW6==="));
    }

    @Test
    void should_decode_foob() {
        assertArrayEquals("foob".getBytes(), Base32.decode("MZXW6YQ="));
    }

    @Test
    void should_decode_fooba() {
        assertArrayEquals("fooba".getBytes(), Base32.decode("MZXW6YTB"));
    }

    @Test
    void should_decode_foobar() {
        assertArrayEquals(
            "foobar".getBytes(),
            Base32.decode("MZXW6YTBOI======")
        );
    }

    @Test
    void should_encode_f() {
        assertEquals("MY======", Base32.encode("f".getBytes()));
    }

    @Test
    void should_encode_fo() {
        assertEquals("MZXQ====", Base32.encode("fo".getBytes()));
    }

    @Test
    void should_encode_foo() {
        assertEquals("MZXW6===", Base32.encode("foo".getBytes()));
    }

    @Test
    void should_encode_foob() {
        assertEquals("MZXW6YQ=", Base32.encode("foob".getBytes()));
    }

    @Test
    void should_encode_fooba() {
        assertEquals("MZXW6YTB", Base32.encode("fooba".getBytes()));
    }

    @Test
    void should_encode_foobar() {
        assertEquals("MZXW6YTBOI======", Base32.encode("foobar".getBytes()));
    }

    @Test
    void should_roundtrip() {
        String original = "The quick brown fox jumps over the lazy dog";
        assertEquals(
            original,
            new String(Base32.decode(Base32.encode(original.getBytes())))
        );
    }

    @Test
    void should_roundtrip_all_byte_values() {
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) all[i] = (byte) i;
        assertArrayEquals(all, Base32.decode(Base32.encode(all)));
    }

    @Test
    void should_roundtrip_various_lengths() {
        for (int len : new int[] {
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            15,
            16,
            31,
            32,
            33,
            64,
        }) {
            byte[] data = new byte[len];
            for (int i = 0; i < len; i++) data[i] = (byte) (i * 17);
            assertArrayEquals(
                data,
                Base32.decode(Base32.encode(data)),
                "roundtrip failed for length " + len
            );
        }
    }

    @Test
    void should_encode_hello() {
        assertEquals("JBSWY3DPEE======", Base32.encode("Hello!".getBytes()));
    }

    @Test
    void should_encode_empty() {
        assertEquals("", Base32.encode(new byte[0]));
    }

    @Test
    void should_encode_null_as_empty() {
        assertEquals("", Base32.encode(null));
    }

    @Test
    void should_decode_hello() {
        assertArrayEquals("Hello!".getBytes(), Base32.decode("JBSWY3DPEE"));
    }

    @Test
    void should_decode_with_padding() {
        assertArrayEquals("f".getBytes(), Base32.decode("MY======"));
    }

    @Test
    void should_decode_lowercase() {
        assertArrayEquals("Hello!".getBytes(), Base32.decode("jbswy3dpee"));
    }

    @Test
    void should_decode_with_spaces() {
        assertArrayEquals("Hello!".getBytes(), Base32.decode("JBS WY3 DPEE"));
    }

    @Test
    void should_decode_with_hyphens() {
        assertArrayEquals("Hello!".getBytes(), Base32.decode("JBSW-Y3D-PEE"));
    }

    @Test
    void should_decode_empty_string() {
        assertArrayEquals(new byte[0], Base32.decode(""));
    }

    @Test
    void should_return_null_for_null_input() {
        assertNull(Base32.decode(null));
    }

    @Test
    void should_throw_on_invalid_character() {
        assertThrows(IllegalArgumentException.class, () ->
            Base32.decode("JBS!Y3DPEE")
        );
    }

    @Test
    void should_throw_on_invalid_character_digit() {
        assertThrows(IllegalArgumentException.class, () ->
            Base32.decode("JBS0Y3DPEE")
        );
    }

    @Test
    void should_validate_base32() {
        assertTrue(Base32.isValidBase32("JBSWY3DPEHPK3PXP"));
    }

    @Test
    void should_validate_with_padding() {
        assertTrue(Base32.isValidBase32("MZXW6YTBOI======"));
    }

    @Test
    void should_invalidate_with_special_chars() {
        assertFalse(Base32.isValidBase32("JBSWY3D!EHPK3PXP"));
    }

    @Test
    void should_invalidate_empty() {
        assertFalse(Base32.isValidBase32(""));
    }

    @Test
    void should_invalidate_null() {
        assertFalse(Base32.isValidBase32(null));
    }

    @Test
    void should_validate_lowercase() {
        assertTrue(Base32.isValidBase32("jbswy3dpee"));
    }

    @Test
    void should_invalidate_wrong_padding() {
        assertFalse(Base32.isValidBase32("JBSWY===DPEE"));
    }
}
