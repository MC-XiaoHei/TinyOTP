package cn.xor7.xiaohei.tinyotp.core;

import static org.junit.jupiter.api.Assertions.*;

import cn.xor7.xiaohei.tinyotp.crypto.MemoryGuard;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TotpGeneratorTest {

    private final TotpGenerator generator = new TotpGenerator();

    private static byte[] testKey() {
        return "12345678901234567890".getBytes();
    }

    @Test
    void should_generate_6_digit_code() {
        String code = generator.generateCode(testKey(), Instant.now());
        assertEquals(6, code.length());
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
    void should_erase_rawKey_after_generation() {
        byte[] key = testKey();
        generator.generateCode(key, Instant.now());
        boolean allZero = true;
        for (byte b : key) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        assertTrue(allZero, "key should be zeroed after generateCode");
    }
}
