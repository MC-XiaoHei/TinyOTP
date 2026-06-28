package cn.xor7.xiaohei.tinyotp.core;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class OtpAuthParser {

    private OtpAuthParser() {}

    public static OtpAuthResult parse(String uri) {
        if (uri == null || !uri.startsWith("otpauth://")) {
            throw new IllegalArgumentException("Invalid otpauth URI");
        }
        String withoutScheme = uri.substring("otpauth://".length());
        int slashIndex = withoutScheme.indexOf('/');
        if (slashIndex == -1) {
            throw new IllegalArgumentException("Missing type in otpauth URI");
        }
        String type = withoutScheme.substring(0, slashIndex);
        if (!"totp".equals(type) && !"hotp".equals(type)) {
            throw new IllegalArgumentException("Unsupported otpauth type: " + type);
        }
        String pathAndQuery = withoutScheme.substring(slashIndex + 1);
        int queryIndex = pathAndQuery.indexOf('?');
        if (queryIndex == -1) {
            throw new IllegalArgumentException("Missing query parameters in otpauth URI");
        }
        String label = pathAndQuery.substring(0, queryIndex);
        String queryString = pathAndQuery.substring(queryIndex + 1);
        String issuer = "";
        String account = "";
        int colonIndex = label.indexOf(':');
        if (colonIndex != -1) {
            issuer = decodeUrl(label.substring(0, colonIndex));
            account = decodeUrl(label.substring(colonIndex + 1));
        } else {
            account = decodeUrl(label);
        }
        String secret = null;
        String algorithm = "SHA1";
        int digits = 6;
        int period = 30;
        long counter = 0;
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex == -1) continue;
            String key = pair.substring(0, eqIndex).toLowerCase();
            String value = decodeUrl(pair.substring(eqIndex + 1));
            switch (key) {
                case "secret":
                    secret = value;
                    break;
                case "issuer":
                    if (issuer.isEmpty()) {
                        issuer = value;
                    }
                    break;
                case "algorithm":
                    algorithm = value.toUpperCase();
                    break;
                case "digits":
                    digits = Integer.parseInt(value);
                    break;
                case "period":
                    period = Integer.parseInt(value);
                    break;
                case "counter":
                    counter = Long.parseLong(value);
                    break;
            }
        }
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Missing secret in otpauth URI");
        }
        if (!Base32.isValidBase32(secret)) {
            throw new IllegalArgumentException("Invalid Base32 secret in otpauth URI");
        }
        return new OtpAuthResult(type, issuer, account, secret, algorithm, digits, period, counter);
    }

    private static String decodeUrl(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public static final class OtpAuthResult {
        private final String type;
        private final String issuer;
        private final String account;
        private final String secret;
        private final String algorithm;
        private final int digits;
        private final int period;
        private final long counter;

        public OtpAuthResult(String type, String issuer, String account, String secret,
                             String algorithm, int digits, int period, long counter) {
            this.type = type;
            this.issuer = issuer;
            this.account = account;
            this.secret = secret;
            this.algorithm = algorithm;
            this.digits = digits;
            this.period = period;
            this.counter = counter;
        }

        public String getType() { return type; }
        public String getIssuer() { return issuer; }
        public String getAccount() { return account; }
        public String getSecret() { return secret; }
        public String getAlgorithm() { return algorithm; }
        public int getDigits() { return digits; }
        public int getPeriod() { return period; }
        public long getCounter() { return counter; }
    }
}
