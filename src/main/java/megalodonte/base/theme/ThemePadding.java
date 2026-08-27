package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

/**
 * Padding scale for a theme. Provides default padding values for components.
 * All values are automatically scaled by {@link ScaleProvider} to match the display DPI.
 *
 * @param xs extra-small padding
 * @param sm small padding
 * @param md medium padding (default fallback for {@link megalodonte.props.Paddable})
 * @param lg large padding
 * @param xl extra-large padding
 */
public record ThemePadding(
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
