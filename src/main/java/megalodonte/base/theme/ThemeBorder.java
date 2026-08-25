package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

/**
 * Border configuration for a theme. Defines default border width and a radius
 * scale for rounded corners. All values are automatically scaled by
 * {@link ScaleProvider} to match the display DPI.
 *
 * @param width    default border width in pixels (scaled)
 * @param radiusSm small corner radius (scaled)
 * @param radiusMd medium corner radius (scaled)
 * @param radiusLg large corner radius (scaled)
 */
public record ThemeBorder(
        int width,
        int radiusSm,
        int radiusMd,
        int radiusLg
) {
    @Override public int width() { return ScaleProvider.scale(width); }
    @Override public int radiusSm() { return ScaleProvider.scale(radiusSm); }
    @Override public int radiusMd() { return ScaleProvider.scale(radiusMd); }
    @Override public int radiusLg() { return ScaleProvider.scale(radiusLg); }
}
