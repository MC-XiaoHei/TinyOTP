package cn.xor7.xiaohei.tinyotp.crypto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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

    @Test
    void should_throw_on_protect_after_destroy() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        mgr.destroy();
        assertThrows(IllegalStateException.class, () ->
            mgr.protect("data".getBytes())
        );
    }

    @Test
    void should_handle_multiple_protect_expose_cycles() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        for (int i = 0; i < 10; i++) {
            String data = "cycle-" + i;
            byte[] blob = mgr.protect(data.getBytes());
            byte[] exposed = mgr.expose(blob);
            assertArrayEquals(data.getBytes(), exposed);
            MemoryGuard.erase(exposed);
        }
    }

    @Test
    void should_produce_different_blobs_for_same_data() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        byte[] data = "same-data".getBytes();
        byte[] blob1 = mgr.protect(data.clone());
        byte[] blob2 = mgr.protect(data.clone());
        assertFalse(java.util.Arrays.equals(blob1, blob2));
    }

    @Test
    void should_handle_empty_data() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        byte[] blob = mgr.protect(new byte[0]);
        byte[] exposed = mgr.expose(blob);
        assertArrayEquals(new byte[0], exposed);
    }

    @Test
    void should_handle_large_data() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        byte[] large = new byte[10_000];
        for (int i = 0; i < large.length; i++) large[i] = (byte) (i & 0xFF);
        byte[] blob = mgr.protect(large);
        byte[] exposed = mgr.expose(blob);
        assertArrayEquals(large, exposed);
        MemoryGuard.erase(exposed);
    }

    @Test
    void should_support_multiple_exposes_of_same_blob() {
        SessionKeyManager mgr = new SessionKeyManager();
        mgr.initialize();
        byte[] data = "multi-expose".getBytes();
        byte[] blob = mgr.protect(data.clone());
        byte[] e1 = mgr.expose(blob);
        byte[] e2 = mgr.expose(blob);
        assertArrayEquals(data, e1);
        assertArrayEquals(data, e2);
        MemoryGuard.erase(e1);
        MemoryGuard.erase(e2);
    }
}
