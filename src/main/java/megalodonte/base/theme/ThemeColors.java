package megalodonte.base.theme;

public record ThemeColors(
        String background,
        String surface,
        String primary,
        String secondary,
        String textPrimary,
        String textSecondary,
        String border,
        // Button colors
        String buttonPrimary,
        String buttonSecondary,
        String buttonSuccess,
        String buttonWarning,
        String buttonDanger,
        String buttonGhost,
        String buttonDisabled
) {}
