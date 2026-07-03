package megalodonte.base.theme;

import megalodonte.base.scale.ScaleProvider;
import megalodonte.utils.related.TextVariant;

public record ThemeTypography(int title, int subtitle, int body, int small) {

    @Override
    public int title() { return ScaleProvider.scale(title); }

    @Override
    public int subtitle() { return ScaleProvider.scale(subtitle); }

    @Override
    public int body() { return ScaleProvider.scale(body); }

    @Override
    public int small() { return ScaleProvider.scale(small); }

    public int resolve(TextVariant variant) {
        return ScaleProvider.scale(switch (variant) {
            case TITLE -> title;
            case SUBTITLE -> subtitle;
            case BODY -> body;
            case SMALL -> small;
        });
    }
}