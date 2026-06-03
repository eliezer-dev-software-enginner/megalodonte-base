package megalodonte.base.theme;

/**
 * Contract for a theme. Implement this to create a custom theme.
 *
 * <pre>{@code
 * public class MyTheme implements ThemeInterface {
 *     public ThemeColors colors() { return new ThemeColors(...); }
 *     public ThemeTypography typography() { return new ThemeTypography(...); }
 *     public ThemeSpacing spacing() { return new ThemeSpacing(...); }
 *     public ThemeBorder border() { return new ThemeBorder(...); }
 * }
 *
 * // Apply it:
 * ThemeInterface.useTheme(new MyTheme());
 * }</pre>
 */
public interface ThemeInterface {
    ThemeColors colors();
    ThemeTypography typography();
    ThemeSpacing spacing();
    ThemeBorder border();

    /** Apply a theme. Delegates to the active theme manager (e.g. megalodonte-theme). */
    static void useTheme(ThemeInterface theme) {
        ThemeHolder.apply(theme);
    }
}
