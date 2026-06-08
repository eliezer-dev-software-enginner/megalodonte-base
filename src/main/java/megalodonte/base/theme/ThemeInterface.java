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
 * // Apply it via ThemeManager (from megalodonte-theme):
 * ThemeManager.setTheme(new MyTheme());
 * }</pre>
 */
public interface ThemeInterface {
    ThemeColors colors();
    ThemeTypography typography();
    ThemeSpacing spacing();
    ThemeBorder border();
}
