package cn.xor7.xiaohei.tinyotp.core;

import static org.junit.jupiter.api.Assertions.*;

import cn.xor7.xiaohei.tinyotp.crypto.MemoryGuard;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TotpGeneratorTest {

    private final TotpGenerator generator = new TotpGenerator();

    @Test
    void should_generate_rfc6238_sha1_t59() {
        byte[] key = hex("3132333435363738393031323334353637383930");
        assertEquals(
            "287082",
            generator.generateCode(key.clone(), Instant.ofEpochSecond(59))
        );
    }

    @Test
    void should_generate_rfc6238_sha1_t1111111109() {
        byte[] key = hex("3132333435363738393031323334353637383930");
        assertEquals(
            "081804",
            generator.generateCode(
                key.clone(),
                Instant.ofEpochSecond(1111111109L)
            )
        );
    }

    @Test
    void should_generate_rfc6238_sha1_t20000000000() {
        byte[] key = hex("3132333435363738393031323334353637383930");
        assertEquals(
            "353130",
            generator.generateCode(
                key.clone(),
                Instant.ofEpochSecond(20000000000L)
            )
        );
    }

    @Test
    void should_generate_6_digit_code() {
        assertEquals(
            6,
            generator.generateCode(testKey(), Instant.now()).length()
        );
    }

    @Test
    void should_generate_different_codes_at_different_times() {
        String code1 = generator.generateCode(
            testKey(),
            Instant.ofEpochSecond(1000)
        );
        String code2 = generator.generateCode(
            testKey(),
            Instant.ofEpochSecond(2000)
        );
        assertNotEquals(code1, code2);
    }

    @Test
    void should_generate_same_code_within_same_time_step() {
        byte[] key = testKey();
        assertEquals(
            generator.generateCode(key.clone(), Instant.ofEpochSecond(1000)),
            generator.generateCode(key.clone(), Instant.ofEpochSecond(1015))
        );
    }

    @Test
    void should_generate_different_code_across_time_step_boundary() {
        byte[] key = testKey();
        assertNotEquals(
            generator.generateCode(key.clone(), Instant.ofEpochSecond(29)),
            generator.generateCode(key.clone(), Instant.ofEpochSecond(30))
        );
    }

    @Test
    void should_generate_code_at_epoch_zero() {
        assertEquals(
            6,
            generator.generateCode(testKey(), Instant.ofEpochSecond(0)).length()
        );
    }

    @Test
    void should_handle_large_counter() {
        assertEquals(
            6,
            generator
                .generateCode(testKey(), Instant.ofEpochSecond(9999999999L))
                .length()
        );
    }

    @Test
    void should_erase_rawKey_after_generation() {
        byte[] key = testKey();
        generator.generateCode(key, Instant.now());
        for (byte b : key) {
            assertEquals((byte) 0, b);
        }
    }

    @Test
    void should_be_deterministic() {
        Instant t = Instant.ofEpochSecond(123456);
        assertEquals(
            generator.generateCode(testKey(), t),
            generator.generateCode(testKey(), t)
        );
    }

    private static byte[] testKey() {
        return "12345678901234567890".getBytes();
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
