package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

/**
 * @param fontFamily Name of the font family the application should use (e.g. "Roboto").
 *                    {@code null} means "don't override" — falls back to the platform's
 *                    default UI font. The theme only names the font; loading the actual
 *                    font file (if it's not already installed on the OS) is up to the
 *                    application, e.g. via {@code javafx.scene.text.Font.loadFont(...)}
 *                    at startup, before the family name becomes usable.
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
