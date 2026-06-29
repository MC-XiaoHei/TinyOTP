package cn.xor7.xiaohei.tinyotp.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OtpAuthParserTest {

    @Test
    void should_parse_full_uri() {
        String uri =
            "otpauth://totp/GitHub:bob@example.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&algorithm=SHA1&digits=6&period=30";
        OtpAuthParser.OtpAuthResult result = OtpAuthParser.parse(uri);
        assertEquals("GitHub", result.getIssuer());
        assertEquals("bob@example.com", result.getAccount());
        assertEquals("JBSWY3DPEHPK3PXP", result.getSecret());
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
    void should_throw_on_unsupported_type() {
        String uri = "otpauth://hotp/Label:user@x.com?secret=JBSWY3DPEHPK3PXP";
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse(uri)
        );

        String uri2 = "otpauth://unknown/user?secret=JBSWY3DPEHPK3PXP";
        assertThrows(IllegalArgumentException.class, () ->
            OtpAuthParser.parse(uri2)
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
