package cn.xor7.xiaohei.tinyotp.core;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class HmacSha1 {
    private static final String ALGORITHM = "HmacSHA1";
    private static final int OUTPUT_LENGTH = 20;

    private HmacSha1() {}

    public static byte[] sign(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            mac.init(keySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA1 not available", e);
        }
    }
}
