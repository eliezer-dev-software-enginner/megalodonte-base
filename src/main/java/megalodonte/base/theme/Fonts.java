package megalodonte.base.theme;

import java.util.Map;

/** Resolves a base font family + CSS weight token into the family name that must
 *  actually be registered via {@link FontLoader} for that weight to render —
 *  since JavaFX only synthesizes Regular/Bold, not intermediate weights. */
public final class Fonts {
    private static final Map<String, String> WEIGHT_SUFFIXES = Map.of(
            "100", "Thin", "200", "ExtraLight", "300", "Light",
            "400", "Regular", "500", "Medium", "600", "SemiBold",
            "700", "Bold", "800", "ExtraBold", "900", "Black"
    );

    private Fonts() {}

    /** e.g. ("Inter", "600") -> "Inter SemiBold". Falls back to {@code baseFamily}
     *  unchanged if the weight is unrecognized, "normal", "bold", or blank. */
    public static String resolveWeightedFamily(String baseFamily, String weight) {
        if (baseFamily == null || baseFamily.isBlank() || weight == null || weight.isBlank()) {
            return baseFamily;
        }
        String suffix = WEIGHT_SUFFIXES.get(weight.trim());
        if (suffix == null || suffix.equals("Regular")) return baseFamily;
        return baseFamily + " " + suffix;
    }
}