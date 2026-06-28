package cn.xor7.xiaohei.tinyotp.storage;

public final class VaultFileFormat {

    public static final byte[] MAGIC = { 'T', 'O', 'T', 'P' };
    public static final int VERSION = 0x0002;
    public static final int SALT_LENGTH = 16;
    public static final int IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    public static final int MEMORY_COST = 16384;
    public static final int ITERATIONS = 2;
    public static final int PARALLELISM = 1;

    private VaultFileFormat() {}
}
