package megalodonte.base.theme;

import megalodonte.utils.related.TextVariant;

public record ThemeTypography(int title, int subtitle, int body, int small) {

    public int resolve(TextVariant variant) {
        return switch (variant) {
            case TITLE -> title;
            case SUBTITLE -> subtitle;
            case BODY -> body;
            case SMALL -> small;
        };
    }
}