package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

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
