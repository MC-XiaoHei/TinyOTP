package cn.xor7.xiaohei.tinyotp.service;

import cn.xor7.xiaohei.tinyotp.core.Base32;
import cn.xor7.xiaohei.tinyotp.core.TotpGenerator;
import cn.xor7.xiaohei.tinyotp.crypto.*;
import cn.xor7.xiaohei.tinyotp.model.*;
import cn.xor7.xiaohei.tinyotp.platform.PlatformProvider;
import cn.xor7.xiaohei.tinyotp.storage.VaultFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VaultService {

    private static final Logger log = LoggerFactory.getLogger(
        VaultService.class
    );

    private final Path vaultPath;
    private final Path keyPath;
    private final VaultFile vaultFile;
    private final SessionKeyManager sessionKey;
    private final TotpGenerator totpGenerator;
    private VaultData currentData;

    public VaultService(Path vaultPath) {
        this.vaultPath = vaultPath;
        this.keyPath = vaultPath.resolveSibling("vault.key");
        this.vaultFile = new VaultFile(vaultPath);
        this.sessionKey = new SessionKeyManager();
        this.totpGenerator = new TotpGenerator();
        this.currentData = new VaultData();
    }

    public boolean isFirstTime() {
        if (!vaultFile.exists()) return true;
        return !Files.exists(keyPath);
    }

    public boolean isUnlocked() {
        return sessionKey.isActive();
    }

    // ── Hello verify (must call on FX thread) ──

    public boolean verifyWithHello() {
        return PlatformProvider.verifyHello("TinyOTP");
    }

    // ── Setup (call verifyWithHello on FX thread first) ──

    public void createAndEnableHello() throws Exception {
        log.info("createAndEnableHello: start");
        // clean old vault if present
        if (vaultFile.exists()) {
            log.info("deleting old vault");
            Files.delete(vaultPath);
        }

        // generate random Argon2id password
        byte[] randomBytes = SecureRandomGenerator.generateBytes(32);
        String randomPassword = Base64.getEncoder().encodeToString(randomBytes);
        MemoryGuard.erase(randomBytes);
        log.info("random password generated");

        // DPAPI-protect the password and save to .key file
        byte[] encrypted = PlatformProvider.dpapiProtect(
            randomPassword.getBytes(StandardCharsets.UTF_8)
        );
        Files.createDirectories(keyPath.getParent());
        Files.write(keyPath, encrypted);
        log.info("key file written to {}", keyPath);

        // create vault
        char[] pwdChars = randomPassword.toCharArray();
        currentData = new VaultData();
        currentData.setConfig(new VaultConfig());
        vaultFile.create(pwdChars, currentData);
        log.info("vault file created");

        // unlock (sets cachedMasterKey)
        unlock(pwdChars);
        MemoryGuard.erase(pwdChars);

        log.info("vault created with Hello (key={})", keyPath);
    }

    // ── Unlock (call verifyWithHello on FX thread first) ──

    public void unlock(char[] password) throws Exception {
        VaultData loaded = vaultFile.open(password);
        sessionKey.initialize();
        List<TotpEntry> valid = new ArrayList<>();
        for (TotpEntry entry : loaded.getEntries()) {
            String s = entry.getSecret();
            if (s != null && !s.isEmpty()) {
                byte[] rawKey = Base32.decode(s);
                byte[] blob = sessionKey.protect(rawKey);
                entry.setSecretBlob(blob);
                entry.setSecret(null);
                MemoryGuard.erase(rawKey);
                valid.add(entry);
            }
        }
        loaded.getEntries().clear();
        loaded.getEntries().addAll(valid);
        this.currentData = loaded;
        log.info("unlocked: {} entries", valid.size());
    }

    public void unlockWithHello() throws Exception {
        log.info("unlockWithHello: start");
        if (!Files.exists(keyPath)) {
            log.warn("key file not found: {}", keyPath);
            throw new IllegalStateException("Key file not found at " + keyPath);
        }

        // DPAPI decrypt the random password from .key file
        byte[] encrypted = Files.readAllBytes(keyPath);
        byte[] plainPwd = PlatformProvider.dpapiUnprotect(encrypted);
        String randomPassword = new String(plainPwd, StandardCharsets.UTF_8);
        MemoryGuard.erase(plainPwd);
        log.info("password recovered from key file");

        unlock(randomPassword.toCharArray());
        MemoryGuard.erase(randomPassword.toCharArray());

        log.info("hello unlock: {} entries", currentData.getEntries().size());
    }

    // ── CRUD ──

    public List<TotpEntry> getEntries() {
        return currentData != null ? currentData.getEntries() : List.of();
    }

    public VaultConfig getConfig() {
        return currentData != null
            ? currentData.getConfig()
            : new VaultConfig();
    }

    public void addEntry(TotpEntry entry) throws Exception {
        if (entry.getSecret() != null && !entry.getSecret().isEmpty()) {
            byte[] rawKey = Base32.decode(entry.getSecret());
            byte[] blob = sessionKey.protect(rawKey);
            entry.setSecretBlob(blob);
            entry.setSecret(null);
            MemoryGuard.erase(rawKey);
        }
        currentData.getEntries().add(entry);
        save();
    }

    public void updateEntry(TotpEntry entry) throws Exception {
        if (entry.getSecret() != null && !entry.getSecret().isEmpty()) {
            byte[] rawKey = Base32.decode(entry.getSecret());
            byte[] blob = sessionKey.protect(rawKey);
            entry.setSecretBlob(blob);
            entry.setSecret(null);
            MemoryGuard.erase(rawKey);
        }
        save();
    }

    public void deleteEntry(TotpEntry entry) throws Exception {
        currentData.getEntries().remove(entry);
        entry.clearSecret();
        save();
    }

    public String generateCode(TotpEntry entry) {
        byte[] rawKey = sessionKey.expose(entry.getSecretBlob());
        try {
            return totpGenerator.generateCode(rawKey, Instant.now());
        } finally {
            MemoryGuard.erase(rawKey);
        }
    }

    // ── Persist ──

    private void save() throws Exception {
        List<TotpEntry> storage = new ArrayList<>();
        for (TotpEntry entry : currentData.getEntries()) {
            if (entry.getSecretBlob() == null) continue;
            TotpEntry copy = new TotpEntry();
            copy.setIssuer(entry.getIssuer());
            copy.setAccount(entry.getAccount());
            copy.setIconColor(entry.getIconColor());
            copy.setSortOrder(entry.getSortOrder());
            copy.setCreatedAt(entry.getCreatedAt());
            copy.setUpdatedAt(System.currentTimeMillis());
            byte[] rawKey = sessionKey.expose(entry.getSecretBlob());
            copy.setSecret(Base32.encode(rawKey));
            MemoryGuard.erase(rawKey);
            storage.add(copy);
        }
        VaultData data = new VaultData();
        data.setEntries(storage);
        data.setConfig(currentData.getConfig());
        vaultFile.persist(data);
    }

    // ── Windows Hello availability ──

    public boolean isHelloAvailable() {
        return PlatformProvider.isHelloAvailable();
    }

    public boolean isHelloConfigured() {
        return isHelloAvailable() && Files.exists(keyPath);
    }

    // ── Backup ──

    public void exportBackup(Path dest) throws Exception {
        vaultFile.exportBackup(dest);
    }

    public void importBackup(Path src) throws Exception {
        byte[] masterKey = vaultFile.getCachedMasterKey();
        if (masterKey == null) {
            throw new IllegalStateException("Not unlocked");
        }
        try {
            VaultFile backupFile = new VaultFile(src);
            VaultData imported = backupFile.openWithMasterKey(masterKey);
            sessionKey.initialize();
            List<TotpEntry> valid = new ArrayList<>();
            for (TotpEntry entry : imported.getEntries()) {
                String s = entry.getSecret();
                if (s != null && !s.isEmpty()) {
                    byte[] rawKey = Base32.decode(s);
                    byte[] blob = sessionKey.protect(rawKey);
                    entry.setSecretBlob(blob);
                    entry.setSecret(null);
                    MemoryGuard.erase(rawKey);
                    valid.add(entry);
                }
            }
            imported.getEntries().clear();
            imported.getEntries().addAll(valid);
            this.currentData = imported;
        } finally {
            MemoryGuard.erase(masterKey);
        }
    }
}
