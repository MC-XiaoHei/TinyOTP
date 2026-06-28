package cn.xor7.xiaohei.tinyotp.storage;

import cn.xor7.xiaohei.tinyotp.crypto.*;
import cn.xor7.xiaohei.tinyotp.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VaultFile {

    private static final Logger log = LoggerFactory.getLogger(VaultFile.class);

    private static final String VERIFIER_PLAINTEXT =
        "TINYOTP_VAULT_VERIFIER_0123456789";

    private final Path filePath;
    private final ObjectMapper objectMapper;

    private byte[] cachedSalt;
    private int cachedMemory;
    private int cachedIterations;
    private int cachedParallelism;
    private byte[] cachedMasterKey;

    public VaultFile(Path filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    public boolean exists() {
        return Files.exists(filePath);
    }

    public void create(char[] password, VaultData data) throws Exception {
        Files.createDirectories(filePath.getParent());
        byte[] salt = SecureRandomGenerator.generateSalt();
        byte[] masterKey = MasterKeyDeriver.deriveKey(password, salt);
        try {
            byte[] payload = objectMapper.writeValueAsBytes(data);
            byte[] verifierPlain = VERIFIER_PLAINTEXT.getBytes(
                StandardCharsets.UTF_8
            );
            SecretCipher.EncryptedData verifierEnc = SecretCipher.encrypt(
                verifierPlain,
                masterKey
            );
            SecretCipher.EncryptedData payloadEnc = SecretCipher.encrypt(
                payload,
                masterKey
            );
            writeFile(
                salt,
                VaultFileFormat.MEMORY_COST,
                VaultFileFormat.ITERATIONS,
                VaultFileFormat.PARALLELISM,
                verifierEnc,
                payloadEnc
            );
            this.cachedSalt = salt;
            this.cachedMemory = VaultFileFormat.MEMORY_COST;
            this.cachedIterations = VaultFileFormat.ITERATIONS;
            this.cachedParallelism = VaultFileFormat.PARALLELISM;
        } finally {
            MemoryGuard.erase(masterKey);
        }
    }

    public VaultData open(char[] password) throws Exception {
        byte[] masterKey = MasterKeyDeriver.deriveKey(password, readSalt());
        try {
            this.cachedMasterKey = masterKey.clone();
            return decryptWithMasterKey(masterKey);
        } finally {
            MemoryGuard.erase(masterKey);
        }
    }

    public VaultData openWithMasterKey(byte[] masterKey) throws Exception {
        readHeader();
        this.cachedMasterKey = masterKey.clone();
        return decryptWithMasterKey(masterKey);
    }

    public byte[] deriveMasterKey(char[] password) {
        if (cachedSalt == null) {
            throw new IllegalStateException(
                "Vault not opened yet, no cached salt available"
            );
        }
        return MasterKeyDeriver.deriveKey(password, cachedSalt);
    }

    public void saveWithMasterKey(byte[] masterKey, VaultData data)
        throws Exception {
        byte[] payload = objectMapper.writeValueAsBytes(data);
        byte[] verifierPlain = VERIFIER_PLAINTEXT.getBytes(
            StandardCharsets.UTF_8
        );
        SecretCipher.EncryptedData verifierEnc = SecretCipher.encrypt(
            verifierPlain,
            masterKey
        );
        SecretCipher.EncryptedData payloadEnc = SecretCipher.encrypt(
            payload,
            masterKey
        );
        writeFile(
            cachedSalt,
            cachedMemory,
            cachedIterations,
            cachedParallelism,
            verifierEnc,
            payloadEnc
        );
    }

    private byte[] readSalt() throws Exception {
        byte[] fileBytes = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(fileBytes);
        skipHeader(buffer);
        byte[] salt = new byte[16];
        buffer.get(salt);
        int memory = buffer.getInt();
        int iterations = buffer.getInt();
        int parallelism = buffer.getInt();
        this.cachedSalt = salt;
        this.cachedMemory = memory;
        this.cachedIterations = iterations;
        this.cachedParallelism = parallelism;
        return salt;
    }

    private void readHeader() throws Exception {
        readSalt();
    }

    private VaultData decryptWithMasterKey(byte[] masterKey) throws Exception {
        byte[] fileBytes = Files.readAllBytes(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(fileBytes);
        skipHeader(buffer);

        buffer.position(buffer.position() + 16 + 4 + 4 + 4);

        byte[] verifierIv = new byte[12];
        buffer.get(verifierIv);
        int verifierCtLen = buffer.getInt();
        byte[] verifierCt = new byte[verifierCtLen];
        buffer.get(verifierCt);
        SecretCipher.EncryptedData verifierEnc = new SecretCipher.EncryptedData(
            verifierIv,
            verifierCt
        );

        byte[] payloadIv = new byte[12];
        buffer.get(payloadIv);
        int payloadCtLen = buffer.getInt();
        byte[] payloadCt = new byte[payloadCtLen];
        buffer.get(payloadCt);

        byte[] verifierPlain = SecretCipher.decrypt(verifierEnc, masterKey);
        if (
            !VERIFIER_PLAINTEXT.equals(
                new String(verifierPlain, StandardCharsets.UTF_8)
            )
        ) {
            throw new IOException("Incorrect master key");
        }
        SecretCipher.EncryptedData payloadEnc = new SecretCipher.EncryptedData(
            payloadIv,
            payloadCt
        );
        byte[] payload = SecretCipher.decrypt(payloadEnc, masterKey);
        return objectMapper.readValue(payload, VaultData.class);
    }

    public void persist(VaultData data) throws Exception {
        if (cachedMasterKey == null) {
            throw new IllegalStateException("No cached master key available");
        }
        log.debug("persisting vault...");
        saveWithMasterKey(cachedMasterKey, data);
        log.info("vault persisted to {}", filePath);
    }

    public byte[] getCachedMasterKey() {
        if (cachedMasterKey == null) return null;
        return cachedMasterKey.clone();
    }

    public void clearCachedMasterKey() {
        if (cachedMasterKey != null) {
            MemoryGuard.erase(cachedMasterKey);
            cachedMasterKey = null;
        }
    }

    private void skipHeader(ByteBuffer buffer) throws IOException {
        byte[] magic = new byte[4];
        buffer.get(magic);
        for (int i = 0; i < 4; i++) {
            if (magic[i] != VaultFileFormat.MAGIC[i]) {
                throw new IOException("Invalid vault file");
            }
        }
        int version = buffer.getShort() & 0xFFFF;
        if (version != VaultFileFormat.VERSION) {
            throw new IOException("Unsupported version: " + version);
        }
    }

    public void save(char[] password, VaultData data) throws Exception {
        byte[] masterKey = MasterKeyDeriver.deriveKey(password, cachedSalt);
        try {
            byte[] payload = objectMapper.writeValueAsBytes(data);
            byte[] verifierPlain = VERIFIER_PLAINTEXT.getBytes(
                StandardCharsets.UTF_8
            );
            SecretCipher.EncryptedData verifierEnc = SecretCipher.encrypt(
                verifierPlain,
                masterKey
            );
            SecretCipher.EncryptedData payloadEnc = SecretCipher.encrypt(
                payload,
                masterKey
            );
            writeFile(
                cachedSalt,
                cachedMemory,
                cachedIterations,
                cachedParallelism,
                verifierEnc,
                payloadEnc
            );
        } finally {
            MemoryGuard.erase(masterKey);
        }
    }

    public boolean verifyPassword(char[] password) {
        try {
            open(password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void exportBackup(Path backupPath) throws Exception {
        Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public VaultData importBackup(Path backupPath, char[] password)
        throws Exception {
        VaultFile backupFile = new VaultFile(backupPath);
        return backupFile.open(password);
    }

    private void writeFile(
        byte[] salt,
        int mem,
        int iter,
        int para,
        SecretCipher.EncryptedData verifier,
        SecretCipher.EncryptedData payload
    ) throws Exception {
        Files.createDirectories(filePath.getParent());
        byte[] verifierCt = verifier.getCiphertextWithTag();
        byte[] verifierIvArr = verifier.getIv();
        byte[] payloadCt = payload.getCiphertextWithTag();
        byte[] payloadIvArr = payload.getIv();

        int totalLen =
            4 +
            2 +
            16 +
            4 +
            4 +
            4 +
            verifierIvArr.length +
            4 +
            verifierCt.length +
            payloadIvArr.length +
            4 +
            payloadCt.length;

        ByteBuffer buf = ByteBuffer.allocate(totalLen);
        buf.put(VaultFileFormat.MAGIC);
        buf.putShort((short) VaultFileFormat.VERSION);
        buf.put(salt);
        buf.putInt(mem);
        buf.putInt(iter);
        buf.putInt(para);
        buf.put(verifierIvArr);
        buf.putInt(verifierCt.length);
        buf.put(verifierCt);
        buf.put(payloadIvArr);
        buf.putInt(payloadCt.length);
        buf.put(payloadCt);
        Files.write(filePath, buf.array());
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
