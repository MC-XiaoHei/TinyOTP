package cn.xor7.xiaohei.tinyotp.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public final class MasterKeyDeriver {

    private static final int MEMORY_COST = 16384;
    private static final int ITERATIONS = 2;
    private static final int PARALLELISM = 1;
    private static final int KEY_LENGTH = 32;

    private MasterKeyDeriver() {}

    public static byte[] deriveKey(char[] password, byte[] salt) {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        Argon2Parameters params = new Argon2Parameters.Builder(
            Argon2Parameters.ARGON2_id
        )
            .withSalt(salt)
            .withMemoryAsKB(MEMORY_COST)
            .withIterations(ITERATIONS)
            .withParallelism(PARALLELISM)
            .build();
        generator.init(params);
        byte[] result = new byte[KEY_LENGTH];
        generator.generateBytes(password, result);
        return result;
    }
}
