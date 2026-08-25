package megalodonte.base.scale;

import javafx.stage.Screen;

/**
 * Display DPI scaling utility. Detects the screen's output scale factor and
 * applies it to pixel values so the UI looks consistent across different
 * display densities.
 * <p>
 * The scale factor is lazily detected on first access and cached. It can be
 * overridden via {@link #setScale(double)} for testing or accessibility.
 */
public final class ScaleProvider {

    private static Double scaleFactor = null;
    private static double referenceDpi = 96.0;

    private ScaleProvider() {}

    /** Initializes the scale factor (no-op if already detected). */
    public static void initialize() {
        if (scaleFactor == null) {
            scaleFactor = detectScale();
        }
    }

    /** Returns the detected or overridden scale factor. */
    public static double factor() {
        if (scaleFactor == null) {
            scaleFactor = detectScale();
        }
        return scaleFactor;
    }

    /** Scales an integer value by the current factor. */
    public static int scale(int value) {
        return (int) Math.round(value * factor());
    }

    /** Scales a double value by the current factor. */
    public static double scale(double value) {
        return value * factor();
    }

    /**
     * Overrides the detected scale factor. Clamped to [0.25, 4.0].
     *
     * @param override the custom scale factor
     */
    public static void setScale(double override) {
        scaleFactor = Math.max(0.25, Math.min(override, 4.0));
    }

    /** Clears the cached scale factor, forcing re-detection on next access. */
    public static void reset() {
        scaleFactor = null;
    }


    private static double detectScale() {
        try {
            Screen screen = Screen.getPrimary();
            double outputScale = screen.getOutputScaleX();

            if (outputScale <= 0) return 1.0;

            return Math.max(0.5, Math.min(outputScale, 3.0));
        } catch (Exception e) {
            return 1.0;
        }
    }

//    private static double detectScale() {
//        try {
//            Screen screen = Screen.getPrimary();
//            double dpi = screen.getDpi();
//            double outputScale = screen.getOutputScaleX();
//
//            if (dpi <= 0 || outputScale <= 0) {
//                return fallbackScale();
//            }
//
//            return Math.max(0.5, Math.min(dpi / referenceDpi / outputScale, 3.0));
//        } catch (Exception e) {
//            return fallbackScale();
//        }
//    }

    private static double fallbackScale() {
        try {
            var tk = java.awt.Toolkit.getDefaultToolkit();
            int awtDpi = tk.getScreenResolution();
            if (awtDpi > 0 && awtDpi != 96) {
                return Math.max(0.5, Math.min((double) awtDpi / referenceDpi, 3.0));
            }
        } catch (Exception ignored) {}
        return 1.0;
    }
}
