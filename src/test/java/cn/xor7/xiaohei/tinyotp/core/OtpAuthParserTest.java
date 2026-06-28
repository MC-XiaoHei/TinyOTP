package cn.xor7.xiaohei.tinyotp.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OtpAuthParserTest {

    @Test
    void should_parse_full_uri() {
        String uri = "otpauth://totp/GitHub:bob@example.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&algorithm=SHA1&digits=6&period=30";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("totp", result.getType());
        assertEquals("GitHub", result.getIssuer());
        assertEquals("bob@example.com", result.getAccount());
        assertEquals("JBSWY3DPEHPK3PXP", result.getSecret());
        assertEquals("SHA1", result.getAlgorithm());
        assertEquals(6, result.getDigits());
        assertEquals(30, result.getPeriod());
    }

    @Test
    void should_use_defaults_for_optional_fields() {
        String uri = "otpauth://totp/Google:alice@gmail.com?secret=JBSWY3DPEHPK3PXP&issuer=Google";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("SHA1", result.getAlgorithm());
        assertEquals(6, result.getDigits());
        assertEquals(30, result.getPeriod());
    }

    @Test
    void should_throw_on_invalid_uri() {
        assertThrows(IllegalArgumentException.class, () -> OtpAuthParser.parse("not-a-uri"));
    }

    @Test
    void should_throw_on_missing_secret() {
        String uri = "otpauth://totp/Test:user@test.com?issuer=Test";
        assertThrows(IllegalArgumentException.class, () -> OtpAuthParser.parse(uri));
    }

    @Test
    void should_parse_issuer_from_label_when_missing() {
        String uri = "otpauth://totp/GitHub:user@test.com?secret=JBSWY3DPEHPK3PXP";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("GitHub", result.getIssuer());
    }
}
