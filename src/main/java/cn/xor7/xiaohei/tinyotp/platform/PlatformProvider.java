package cn.xor7.xiaohei.tinyotp.platform;

import com.github.hstyi.jhello.JHello;
import com.github.hstyi.jhello.UserConsentVerificationResult;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Crypt32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinCrypt;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.awt.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlatformProvider {

    private static final Logger log = LoggerFactory.getLogger(
        PlatformProvider.class
    );

    private PlatformProvider() {}

    private static final boolean IS_WINDOWS = System.getProperty("os.name")
        .toLowerCase()
        .contains("win");

    private static volatile HWND cachedHwnd;

    /**
     * Store the native HWND of the main window so JHello can use it as parent.
     * Call this after the window is visible.
     */
    public static void cacheHwnd(Frame frame) {
        try {
            HWND hwnd = User32.INSTANCE.FindWindow(null, frame.getTitle());
            if (hwnd == null) {
                // Fallback: search by process
                log.warn(
                    "FindWindow returned null for title '{}'",
                    frame.getTitle()
                );
            } else {
                cachedHwnd = hwnd;
                log.info("cached HWND from frame '{}'", frame.getTitle());
            }
        } catch (Exception e) {
            log.warn("cacheHwnd failed", e);
        }
    }

    // ── Windows Hello ──

    public static boolean isHelloAvailable() {
        if (!IS_WINDOWS) return false;
        try {
            boolean avail = JHello.available();
            log.info("JHello.available() = {}", avail);
            return avail;
        } catch (Exception e) {
            log.warn("JHello.available() exception", e);
            return false;
        }
    }

    /**
     * Re-fetch the native HWND of the main window.
     * Call this right before JHello.verify to ensure a valid parent handle.
     */
    public static void refreshHwnd() {
        String title = "TinyOTP";
        try {
            HWND hwnd = User32.INSTANCE.FindWindow(null, title);
            if (hwnd != null) {
                cachedHwnd = hwnd;
                log.info("refreshHwnd: cached HWND for '{}' = {}", title, hwnd);
            } else {
                log.warn(
                    "refreshHwnd: FindWindow returned null for '{}'",
                    title
                );
            }
        } catch (Exception e) {
            log.warn("refreshHwnd failed", e);
        }
    }

    public static boolean verifyHello(String message) {
        if (!IS_WINDOWS) {
            log.warn("verifyHello: not Windows");
            return false;
        }

        // Ensure we have a valid parent HWND so the Hello dialog
        // is correctly owned and appears in front of the main window.
        HWND parentHwnd = cachedHwnd;
        if (parentHwnd == null) {
            log.warn("verifyHello: cachedHwnd is null, attempting refresh");
            refreshHwnd();
            parentHwnd = cachedHwnd;
        }
        if (parentHwnd == null) {
            // Last resort: use the current foreground window
            parentHwnd = User32.INSTANCE.GetForegroundWindow();
            log.warn(
                "verifyHello: using GetForegroundWindow as parent = {}",
                parentHwnd
            );
        }

        log.info("calling JHello.verify(\"{}\", hwnd={})", message, parentHwnd);
        try {
            UserConsentVerificationResult result = JHello.verify(
                message,
                parentHwnd
            );
            log.info("JHello.verify returned {}", result);
            return result == UserConsentVerificationResult.Verified;
        } catch (Exception e) {
            log.error("JHello.verify exception", e);
            return false;
        }
    }

    // ── DPAPI ──

    public static byte[] dpapiProtect(byte[] data) {
        if (!IS_WINDOWS) throw new UnsupportedOperationException(
            "DPAPI requires Windows"
        );
        WinCrypt.DATA_BLOB.ByReference plain =
            new WinCrypt.DATA_BLOB.ByReference();
        plain.cbData = data.length;
        plain.pbData = new com.sun.jna.Memory(data.length);
        plain.pbData.write(0, data, 0, data.length);
        WinCrypt.DATA_BLOB.ByReference cipher =
            new WinCrypt.DATA_BLOB.ByReference();
        try {
            if (
                !Crypt32.INSTANCE.CryptProtectData(
                    plain,
                    null,
                    null,
                    null,
                    null,
                    0x01,
                    cipher
                )
            ) throw new RuntimeException(
                "CryptProtectData failed: " + Native.getLastError()
            );
            return cipher.pbData.getByteArray(0, cipher.cbData);
        } finally {
            if (
                cipher.pbData != null && cipher.cbData > 0
            ) Kernel32.INSTANCE.LocalFree(cipher.pbData);
        }
    }

    public static byte[] dpapiUnprotect(byte[] data) {
        if (!IS_WINDOWS) throw new UnsupportedOperationException(
            "DPAPI requires Windows"
        );
        WinCrypt.DATA_BLOB.ByReference cipher =
            new WinCrypt.DATA_BLOB.ByReference();
        cipher.cbData = data.length;
        cipher.pbData = new com.sun.jna.Memory(data.length);
        cipher.pbData.write(0, data, 0, data.length);
        WinCrypt.DATA_BLOB.ByReference plain =
            new WinCrypt.DATA_BLOB.ByReference();
        try {
            if (
                !Crypt32.INSTANCE.CryptUnprotectData(
                    cipher,
                    null,
                    null,
                    null,
                    null,
                    0x01,
                    plain
                )
            ) throw new RuntimeException(
                "CryptUnprotectData failed: " + Native.getLastError()
            );
            return plain.pbData.getByteArray(0, plain.cbData);
        } finally {
            if (
                plain.pbData != null && plain.cbData > 0
            ) Kernel32.INSTANCE.LocalFree(plain.pbData);
        }
    }
}
