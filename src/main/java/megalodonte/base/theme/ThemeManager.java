package megalodonte.base.theme;

import javafx.scene.Scene;
import megalodonte.base.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Global theme registry. Holds the active {@link ThemeInterface} and provides
 * utility methods to apply theme-level styles (e.g. font family) to scenes.
 * <p>
 * Set the theme once at application startup:
 * <pre>{@code
 * ThemeManager.setTheme(new MyTheme());
 * }</pre>
 */
public class ThemeManager {
    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    private static final State<ThemeInterface> currentTheme = new State<>(null);

    /**
     * Sets the active theme. Notifies all subscribers of the internal
     * {@link State}, allowing components to reactively re-apply styles.
     *
     * @param theme the theme to activate
     */
    public static void setTheme(ThemeInterface theme) {
        log.info("Theme set to {}", theme.getClass().getSimpleName());
        currentTheme.set(theme);
    }

    /** Returns the currently active theme, or {@code null} if none has been set. */
    public static ThemeInterface theme() {
        return currentTheme.get();
    }

    /** Returns the underlying {@link State} for reactive subscriptions. */
    public static State<ThemeInterface> state() {
        return currentTheme;
    }

    /**
     * Attaches a stylesheet to {@code scene} that sets the current theme's
     * {@link ThemeTypography#fontFamily()} on {@code .root}. Does nothing if there's no
     * active theme or the theme doesn't specify a font family (falls back to the
     * platform default in that case).
     * <p>
     * Call this once, right after creating a {@link Scene} — the stylesheet lives on
     * the Scene itself, not on the root Node, so it keeps applying even if the root
     * gets swapped later (e.g. in-place navigation via {@code Scene.setRoot(...)});
     * JavaFX automatically maintains the {@code .root} style class on whatever node is
     * the current root.
     */
    public static void applyFontFamily(Scene scene) {
        if (scene == null) return;

        ThemeInterface theme = theme();
        if (theme == null) return;

        String fontFamily = theme.typography().fontFamily();
        if (fontFamily == null || fontFamily.isBlank()) return;

        log.debug("Applying font family '{}' to scene", fontFamily);
        String css = ".root { -fx-font-family: \"" + fontFamily.replace("\"", "\\\"") + "\"; }";
        String dataUri = "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));

        scene.getStylesheets().add(dataUri);
    }
}
