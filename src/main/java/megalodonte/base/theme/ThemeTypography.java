package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

/**
 * Typography configuration for a theme. Defines font sizes for the type scale
 * and an optional font family name. All sizes are automatically scaled by
 * {@link ScaleProvider} to match the display DPI.
 *
 * @param fontFamily name of the font family (e.g. "Roboto"), or {@code null}
 *                    to use the platform default. The actual font file must be
 *                    loaded before this name becomes usable — see {@link FontLoader}.
 * @param title      font size for titles (scaled)
 * @param subtitle   font size for subtitles (scaled)
 * @param body       font size for body text (scaled)
 * @param small      font size for small/caption text (scaled)
 */
public record ThemeTypography(String fontFamily, int title, int subtitle, int body, int small) {

    /**
     * Convenience constructor for themes that don't customize the font family.
     */
    public ThemeTypography(int title, int subtitle, int body, int small) {
        this(null, title, subtitle, body, small);
    }

    @Override
    public int title() { return ScaleProvider.scale(title); }

    @Override
    public int subtitle() { return ScaleProvider.scale(subtitle); }

    @Override
    public int body() { return ScaleProvider.scale(body); }

    @Override
    public int small() { return ScaleProvider.scale(small); }
}
