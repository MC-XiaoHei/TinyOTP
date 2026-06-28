package cn.xor7.xiaohei.tinyotp.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionKeyManagerTest {

    @Test
    void should_not_be_active_before_init() {
        assertFalse(new SessionKeyManager().isActive());
    }

    @Test
    void should_be_active_after_init() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        assertTrue(mgr.isActive());
    }

    @Test
    void should_not_be_active_after_destroy() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        mgr.destroy();
        assertFalse(mgr.isActive());
    }

    @Test
    void should_protect_and_expose_roundtrip() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        byte[] rawKey = "test-key-data".getBytes();
        byte[] original = rawKey.clone();
        byte[] blob = mgr.protect(rawKey);
        byte[] exposed = mgr.expose(blob);
        assertArrayEquals(original, exposed);
        MemoryGuard.erase(exposed);
    }

    @Test
    void should_throw_on_expose_after_destroy() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        byte[] blob = mgr.protect("test".getBytes());
        mgr.destroy();
        assertThrows(IllegalStateException.class, () -> mgr.expose(blob));
    }
}
