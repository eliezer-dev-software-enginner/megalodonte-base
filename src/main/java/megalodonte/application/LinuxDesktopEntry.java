package megalodonte.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * On Linux, GNOME-based desktop environments (Zorin OS included) resolve the dock/
 * taskbar icon for a running window by matching its WM_CLASS against the
 * StartupWMClass of an <em>installed</em> {@code .desktop} file — not from whatever
 * icon the app itself sets via {@code Stage.getIcons()}. A jpackage-built install
 * ships its own {@code .desktop} file so this is normally a non-issue there, but
 * running straight from a JVM (IDE run button, {@code gradle run}, a plain
 * {@code java -jar}, ...) has no {@code .desktop} file at all, so the dock falls
 * back to a generic icon regardless of what the app does at the JavaFX level.
 * <p>
 * <b>StartupWMClass</b>: on Linux/GTK, Glass reports the window's WM_CLASS as the
 * launched {@link javafx.application.Application} subclass's fully-qualified name —
 * confirmed empirically via {@code xprop WM_CLASS}, not the app's display name or
 * {@code javafx.application.name} (that property does not affect WM_CLASS on this
 * Glass/GTK version). The caller passes that class name in explicitly (see
 * {@link MegalodonteApplication}), so apps that define their own launcher subclass
 * get their own distinct WM_CLASS/taskbar icon instead of colliding on the shared
 * {@link JavaFXHost}'s.
 * <p>
 * This installs a minimal per-user {@code .desktop} entry under
 * {@code ~/.local/share/applications}, re-writing it on every startup (cheap, and
 * avoids ever going stale across framework changes), purely so GNOME's icon lookup
 * has something to match against. It's marked {@code NoDisplay=true}: it isn't meant
 * to be launched from the app grid, only to exist for the WM_CLASS match. No-op on
 * non-Linux platforms, and best-effort — any I/O failure is swallowed since this is a
 * cosmetic dev-experience nicety, never something that should stop the app from
 * starting.
 */
final class LinuxDesktopEntry {
    private LinuxDesktopEntry() {}

    static void ensure(String appName, String iconResourcePath, String startupWmClass) {
        if (!isLinux() || appName == null || iconResourcePath == null || startupWmClass == null) return;

        try {
            String slug = slug(appName);
            Path iconFile = extractIcon(slug, iconResourcePath);
            if (iconFile == null) return;

            Path desktopFile = appDataPath(".local/share/applications", slug + "-dev.desktop");
            Files.createDirectories(desktopFile.getParent());
            Files.writeString(desktopFile, """
                    [Desktop Entry]
                    Type=Application
                    Name=%s (dev)
                    Exec=true
                    Icon=%s
                    StartupWMClass=%s
                    Terminal=false
                    NoDisplay=true
                    """.formatted(appName, iconFile, startupWmClass));
        } catch (IOException ignored) {
            // best effort — nunca impede a aplicação de subir
        }
    }

    private static Path extractIcon(String slug, String resourcePath) throws IOException {
        Path iconFile = appDataPath(".local/share/icons/hicolor/256x256/apps", slug + ".png");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = LinuxDesktopEntry.class.getClassLoader();

        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        try (InputStream in = classLoader.getResourceAsStream(normalized)) {
            if (in == null) return Files.exists(iconFile) ? iconFile : null;
            Files.createDirectories(iconFile.getParent());
            Files.copy(in, iconFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return iconFile;
    }

    private static Path appDataPath(String relativeDir, String fileName) {
        return Path.of(System.getProperty("user.home")).resolve(relativeDir).resolve(fileName);
    }

    private static String slug(String appName) {
        return appName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
    }
}
