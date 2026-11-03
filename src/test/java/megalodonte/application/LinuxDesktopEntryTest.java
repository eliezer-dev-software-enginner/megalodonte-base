package megalodonte.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LinuxDesktopEntryTest {
    @TempDir Path root;

    @Test void changesIconPathWhenBundledImageChanges() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux"));
        String previousHome = System.getProperty("user.home");
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        Path iconResource = root.resolve("icon.png");
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{root.toUri().toURL()}, null)) {
            System.setProperty("user.home", root.toString());
            Thread.currentThread().setContextClassLoader(loader);
            Files.writeString(iconResource, "first image");
            LinuxDesktopEntry.ensure("Backup USB", "/icon.png", "backup_usb.Main$AppHost");
            Path desktop = root.resolve(".local/share/applications/backup-usb-dev.desktop");
            String first = Files.readString(desktop);
            assertTrue(first.contains("StartupWMClass=backup_usb.Main$AppHost"));
            Files.writeString(iconResource, "updated image");
            LinuxDesktopEntry.ensure("Backup USB", "/icon.png", "backup_usb.Main$AppHost");
            String second = Files.readString(desktop);
            assertNotEquals(first, second);
            assertTrue(second.contains("backup-usb-"));
            String icon = second.lines().filter(line -> line.startsWith("Icon=")).findFirst().orElseThrow().substring(5);
            assertEquals("updated image", Files.readString(Path.of(icon)));
        } finally {
            if (previousHome == null) System.clearProperty("user.home"); else System.setProperty("user.home", previousHome);
            Thread.currentThread().setContextClassLoader(previousLoader);
        }
    }
}
