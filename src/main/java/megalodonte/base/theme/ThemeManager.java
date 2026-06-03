package megalodonte.base.theme;

import megalodonte.base.state.State;

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
}
