package cn.xor7.xiaohei.tinyotp.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OtpAuthParserTest {

    @Test
    void should_parse_full_uri() {
        String uri =
            "otpauth://totp/GitHub:bob@example.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&algorithm=SHA1&digits=6&period=30";
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
        String uri =
            "otpauth://totp/Google:alice@gmail.com?secret=JBSWY3DPEHPK3PXP&issuer=Google";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("SHA1", result.getAlgorithm());
        assertEquals(6, result.getDigits());
        assertEquals(30, result.getPeriod());
    }

    @Test
    void should_throw_on_invalid_uri() {
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse("not-a-uri")
        );
    }

    @Test
    void should_throw_on_missing_secret() {
        String uri = "otpauth://totp/Test:user@test.com?issuer=Test";
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse(uri)
        );
    }

    @Test
    void should_parse_issuer_from_label_when_missing() {
        String uri =
            "otpauth://totp/GitHub:user@test.com?secret=JBSWY3DPEHPK3PXP";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("GitHub", result.getIssuer());
    }

    @Test
    void should_parse_hotp_with_counter() {
        String uri =
            "otpauth://hotp/ACME:john@example.com?secret=JBSWY3DPEHPK3PXP&issuer=ACME&counter=42";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("hotp", result.getType());
        assertEquals(42, result.getCounter());
        assertEquals("ACME", result.getIssuer());
        assertEquals("john@example.com", result.getAccount());
    }

    @Test
    void should_default_counter_to_zero_for_hotp() {
        String uri = "otpauth://hotp/Label:user@x.com?secret=JBSWY3DPEHPK3PXP";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("hotp", result.getType());
        assertEquals(0, result.getCounter());
    }

    @Test
    void should_throw_on_unsupported_type() {
        String uri = "otpauth://unknown/user?secret=JBSWY3DPEHPK3PXP";
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse(uri)
        );
    }

    @Test
    void should_not_override_label_issuer_with_query_issuer() {
        String uri =
            "otpauth://totp/LabelIssuer:user@x.com?secret=JBSWY3DPEHPK3PXP&issuer=QueryIssuer";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("LabelIssuer", result.getIssuer());
    }

    @Test
    void should_use_query_issuer_when_label_has_no_issuer() {
        String uri =
            "otpauth://totp/user@x.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("GitHub", result.getIssuer());
        assertEquals("user@x.com", result.getAccount());
    }

    @Test
    void should_ignore_unknown_query_parameters() {
        String uri =
            "otpauth://totp/GitHub:user@x.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&x=1&y=2";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("GitHub", result.getIssuer());
    }

    @Test
    void should_parse_algorithm_sha256_and_sha512() {
        String uriSha256 =
            "otpauth://totp/T:u@x.com?secret=JBSWY3DPEHPK3PXP&algorithm=SHA256";
        assertEquals("SHA256", OtpAuthParser.parse(uriSha256).getAlgorithm());

        String uriSha512 =
            "otpauth://totp/T:u@x.com?secret=JBSWY3DPEHPK3PXP&algorithm=SHA512";
        assertEquals("SHA512", OtpAuthParser.parse(uriSha512).getAlgorithm());
    }

    @Test
    void should_parse_custom_digits() {
        String uri =
            "otpauth://totp/T:u@x.com?secret=JBSWY3DPEHPK3PXP&digits=8";
        assertEquals(8, OtpAuthParser.parse(uri).getDigits());
    }

    @Test
    void should_parse_custom_period() {
        String uri =
            "otpauth://totp/T:u@x.com?secret=JBSWY3DPEHPK3PXP&period=60";
        assertEquals(60, OtpAuthParser.parse(uri).getPeriod());
    }

    @Test
    void should_decode_percent_encoded_label() {
        String uri =
            "otpauth://totp/My%20Company:user%40x.com?secret=JBSWY3DPEHPK3PXP&issuer=My%20Company";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("My Company", result.getIssuer());
        assertEquals("user@x.com", result.getAccount());
    }

    @Test
    void should_throw_on_null_uri() {
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse(null)
        );
    }

    @Test
    void should_throw_on_missing_query() {
        String uri = "otpauth://totp/Label";
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse(uri)
        );
    }
}
