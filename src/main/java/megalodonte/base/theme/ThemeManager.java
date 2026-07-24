package megalodonte.base.theme;

import javafx.scene.Scene;
import megalodonte.base.state.State;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ThemeManager {
    private static final State<ThemeInterface> currentTheme = new State<>(null);

    public static void setTheme(ThemeInterface theme) {
        currentTheme.set(theme);
    }

    public static ThemeInterface theme() {
        return currentTheme.get();
    }

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

        String css = ".root { -fx-font-family: \"" + fontFamily.replace("\"", "\\\"") + "\"; }";
        String dataUri = "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));

        scene.getStylesheets().add(dataUri);
    }
}
