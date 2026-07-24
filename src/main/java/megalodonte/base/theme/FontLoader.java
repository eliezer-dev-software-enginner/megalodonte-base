package megalodonte.base.theme;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Auto-discovers and registers font files with JavaFX, so their family names
 * become usable as {@code -fx-font-family} (and {@link ThemeTypography#fontFamily()})
 * without the application having to load each file by hand.
 * <p>
 * <b>Convention:</b> client applications should place their font files (.ttf/.otf) under
 * a {@code assets/fonts/} resource directory (e.g.
 * {@code src/main/resources/assets/fonts/roboto/Roboto-Regular.ttf}). Subdirectories are
 * scanned recursively, so families can be organized into their own folders. Nothing needs
 * to be registered per file or per family — dropping a font file under that directory is
 * enough for the application to be able to use its family name.
 * <p>
 * This runs automatically once at startup (see {@link Bootstrap#dispatch}), so applications
 * built on this framework normally never need to call it directly. It's safe to call more
 * than once (JavaFX simply re-registers the same family) and it's a no-op — not an error —
 * when the directory doesn't exist, so apps that don't ship custom fonts pay no cost.
 */
public final class FontLoader {

    /** Conventional classpath resource directory client apps should place their fonts under. */
    public static final String DEFAULT_FONTS_DIRECTORY = "assets/fonts";

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".ttf", ".otf");

    private FontLoader() {}

    /**
     * Scans {@link #DEFAULT_FONTS_DIRECTORY} (on every classpath root — app jar, dependency
     * jars, exploded classes dir when running from an IDE) and registers every font file
     * found with {@link Font#loadFont(InputStream, double)}.
     *
     * @return how many font files were successfully registered
     */
    public static int loadAll() {
        return loadAll(DEFAULT_FONTS_DIRECTORY);
    }

    /**
     * Same as {@link #loadAll()}, but scanning a custom resource directory instead of the
     * conventional {@link #DEFAULT_FONTS_DIRECTORY}.
     */
    public static int loadAll(String resourceDirectory) {
        ClassLoader classLoader = resolveClassLoader();
        String normalized = normalize(resourceDirectory);

        try {
            Enumeration<URL> roots = classLoader.getResources(normalized);
            int loaded = 0;
            while (roots.hasMoreElements()) {
                loaded += loadFromRoot(roots.nextElement(), normalized);
            }
            return loaded;
        } catch (IOException e) {
            System.err.println("[FontLoader] Failed to resolve fonts directory '" + normalized + "': " + e.getMessage());
            return 0;
        }
    }

    private static ClassLoader resolveClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader != null ? contextClassLoader : FontLoader.class.getClassLoader();
    }

    private static String normalize(String dir) {
        String noLeadingSlash = dir.startsWith("/") ? dir.substring(1) : dir;
        return noLeadingSlash.endsWith("/") ? noLeadingSlash : noLeadingSlash + "/";
    }

    /**
     * A resource directory can resolve to a plain filesystem path (running from an IDE /
     * exploded classes) or to a path inside a jar (packaged app) — each needs a different
     * scanning strategy, since a "jar:" URL isn't walkable with {@link Files#walk}.
     */
    private static int loadFromRoot(URL root, String resourceDirectory) {
        try {
            return switch (root.getProtocol()) {
                case "file" -> loadFromDirectory(Path.of(root.toURI()));
                case "jar" -> loadFromJar(root, resourceDirectory);
                default -> {
                    System.err.println("[FontLoader] Unsupported resource protocol '" + root.getProtocol() + "' for " + root);
                    yield 0;
                }
            };
        } catch (URISyntaxException e) {
            System.err.println("[FontLoader] Invalid fonts directory URL '" + root + "': " + e.getMessage());
            return 0;
        }
    }

    private static int loadFromDirectory(Path dir) {
        if (!Files.isDirectory(dir)) return 0;

        try (Stream<Path> walk = Files.walk(dir)) {
            return (int) walk
                    .filter(Files::isRegularFile)
                    .filter(file -> hasSupportedExtension(file.getFileName().toString()))
                    .filter(FontLoader::loadFontFile)
                    .count();
        } catch (IOException e) {
            System.err.println("[FontLoader] Failed to scan fonts directory '" + dir + "': " + e.getMessage());
            return 0;
        }
    }

    private static boolean loadFontFile(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return Font.loadFont(in, 12) != null;
        } catch (IOException e) {
            System.err.println("[FontLoader] Failed to load font '" + file + "': " + e.getMessage());
            return false;
        }
    }

    private static int loadFromJar(URL root, String resourceDirectory) {
        try {
            URLConnection connection = root.openConnection();
            if (!(connection instanceof JarURLConnection jarConnection)) return 0;

            try (JarFile jarFile = jarConnection.getJarFile()) {
                int loaded = 0;
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    if (!entry.getName().startsWith(resourceDirectory)) continue;
                    if (!hasSupportedExtension(entry.getName())) continue;

                    try (InputStream in = jarFile.getInputStream(entry)) {
                        if (Font.loadFont(in, 12) != null) loaded++;
                    } catch (IOException e) {
                        System.err.println("[FontLoader] Failed to load font '" + entry.getName() + "': " + e.getMessage());
                    }
                }
                return loaded;
            }
        } catch (IOException e) {
            System.err.println("[FontLoader] Failed to open jar for fonts scan: " + e.getMessage());
            return 0;
        }
    }

    private static boolean hasSupportedExtension(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }
}
