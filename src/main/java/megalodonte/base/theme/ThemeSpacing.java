package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

/**
 * Spacing scale for a theme. Provides consistent spacing values across the UI.
 * All values are automatically scaled by {@link ScaleProvider} to match the display DPI.
 *
 * @param xs extra-small spacing
 * @param sm small spacing
 * @param md medium spacing
 * @param lg large spacing
 * @param xl extra-large spacing
 */
public record ThemeSpacing(
        int xs,
        int sm,
        int md,
        int lg,
        int xl
) {
    @Override public int xs() { return ScaleProvider.scale(xs); }
    @Override public int sm() { return ScaleProvider.scale(sm); }
    @Override public int md() { return ScaleProvider.scale(md); }
    @Override public int lg() { return ScaleProvider.scale(lg); }
    @Override public int xl() { return ScaleProvider.scale(xl); }
}
