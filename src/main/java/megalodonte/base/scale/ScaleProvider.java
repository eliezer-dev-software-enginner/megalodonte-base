package megalodonte.base.scale;

import javafx.stage.Screen;

public final class ScaleProvider {

    private static Double scaleFactor = null;
    private static double referenceDpi = 96.0;

    private ScaleProvider() {}

    public static void initialize() {
        if (scaleFactor == null) {
            scaleFactor = detectScale();
        }
    }

    public static double factor() {
        if (scaleFactor == null) {
            scaleFactor = detectScale();
        }
        return scaleFactor;
    }

    public static int scale(int value) {
        return (int) Math.round(value * factor());
    }

    public static double scale(double value) {
        return value * factor();
    }

    public static void setScale(double override) {
        scaleFactor = Math.max(0.25, Math.min(override, 4.0));
    }

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
