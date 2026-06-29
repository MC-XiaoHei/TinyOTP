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

    public static void cacheHwnd(Frame frame) {
        try {
            HWND hwnd = User32.INSTANCE.FindWindow(null, frame.getTitle());
            if (hwnd == null) {
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

    public static void refreshHwnd() {
        String title = "TinyOTP";
        try {
            HWND hwnd = User32.INSTANCE.FindWindow(null, title);
            if (hwnd != null) {
                cachedHwnd = hwnd;
                log.info("cached HWND for '{}' = {}", title, hwnd);
            } else {
                log.warn("FindWindow returned null for '{}'", title);
            }
        } catch (Exception e) {
            log.warn("refreshHwnd failed", e);
        }
    }

    public static boolean verifyHello(String message) {
        if (!IS_WINDOWS) {
            log.warn("not Windows");
            return false;
        }

        HWND parentHwnd = cachedHwnd;
        if (parentHwnd == null) {
            log.warn("cachedHwnd is null, attempting refresh");
            refreshHwnd();
            parentHwnd = cachedHwnd;
        }
        if (parentHwnd == null) {
            parentHwnd = User32.INSTANCE.GetForegroundWindow();
            log.warn("using GetForegroundWindow as parent = {}", parentHwnd);
        }

        log.info("JHello.verify hwnd={}", parentHwnd);
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
