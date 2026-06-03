package megalodonte.base.theme;

import java.util.function.Consumer;

/**
 * Internal bridge so that {@link ThemeInterface#useTheme(ThemeInterface)}
 * can delegate to whatever module provides the actual theme management.
 * <p>
 * {@code ThemeManager} (in megalodonte-theme) registers itself via
 * {@link #setSetter(Consumer)} when its class loads.
 */
public class ThemeHolder {
    private static Consumer<ThemeInterface> setter;

    public static void setSetter(Consumer<ThemeInterface> setter) {
        ThemeHolder.setter = setter;
    }

    public static void apply(ThemeInterface theme) {
        var s = setter;
        if (s != null) s.accept(theme);
    }
}
