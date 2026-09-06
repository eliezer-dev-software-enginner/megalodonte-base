package megalodonte.base.theme;

/**
 * Core color palette for a theme. Contains semantic color tokens that components
 * map to their own visual styles.
 *
 * @param background  main background color
 * @param surface     elevated surface color (cards, panels)
 * @param primary     primary brand/accent color
 * @param secondary   secondary accent color
 * @param textPrimary primary text color (headings, body)
 * @param textSecondary secondary text color (captions, hints)
 * @param border      default border color
 * @param placeholder placeholder/muted text color
 * @param selection   text/element selection highlight color
 * @param focusRing   focus indicator color
 * @param hover       hover state background color
 * @param success     semantic color for positive/success states and actions
 * @param warning     semantic color for cautionary/warning states and actions
 * @param danger      semantic color for destructive/error states and actions
 */
public record ThemeColors(
        String background,
        String surface,
        String primary,
        String secondary,
        String textPrimary,
        String textSecondary,
        String border,
        String placeholder,
        String selection,
        String focusRing,
        String hover,
        String success,
        String warning,
        String danger
) {}