package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;

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
