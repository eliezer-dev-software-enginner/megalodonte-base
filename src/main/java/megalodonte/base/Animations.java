package megalodonte.base;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import megalodonte.base.components.Component;

/**
 * Pre-built animation utilities for components. Provides common transitions
 * (fade, slide, scale, bounce, shake, etc.) that can be composed with
 * {@link Component.Transition} for screen enter/exit animations.
 */
public class Animations {

    public static Animation rotate(Node node, double fromAngle, double toAngle, Duration duration) {
        javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(duration, node);
        rt.setFromAngle(fromAngle);
        rt.setToAngle(toAngle);
        return rt;
    }

    public static Animation rotate360(Node node) {
        return rotate(node, 0, 360, Duration.millis(500));
    }
    /** Free rotation between two angles. Useful for refresh icons, toggles, expand/collapse arrows. */
    public static Animation rotate(Component c, double fromAngle, double toAngle, Duration duration) {
        javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(duration, c.getJavaFxNode());
        rt.setFromAngle(fromAngle);
        rt.setToAngle(toAngle);
        return rt;
    }

    /** Full 360-degree spin. Good for "refresh"/loading icon on click. */
    public static Animation rotate360(Component c) {
        return rotate(c, 0, 360, Duration.millis(500));
    }
    public static Animation fadeSlide(Component c, boolean entering) {
        return fadeSlide(c, entering, Duration.ZERO);
    }

    public static Animation fadeSlide(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getJavaFxNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getJavaFxNode());

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromX(20);    tt.setToX(0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromX(0);     tt.setToX(20);
        }

        var p = new ParallelTransition(ft, tt);
        p.setDelay(delay);
        return p;
    }

    /** Pure fade without displacement. Good for neutral content swaps (text, icon). */
    public static Animation fade(Component c, boolean entering) {
        return fade(c, entering, Duration.ZERO);
    }

    public static Animation fade(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), c.getJavaFxNode());
        if (entering) {
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
        }
        ft.setDelay(delay);
        return ft;
    }

    /** Slides from/to above — useful for dropdowns, menus, notifications. */
    public static Animation fadeSlideDown(Component c, boolean entering) {
        return fadeSlideDown(c, entering, Duration.ZERO);
    }

    public static Animation fadeSlideDown(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getJavaFxNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getJavaFxNode());

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromY(-15);   tt.setToY(0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromY(0);     tt.setToY(-15);
        }

        var p = new ParallelTransition(ft, tt);
        p.setDelay(delay);
        return p;
    }

    /** Slides from/to below — useful for toasts/popups rising from the bottom of the screen. */
    public static Animation fadeSlideUp(Component c, boolean entering) {
        return fadeSlideUp(c, entering, Duration.ZERO);
    }

    public static Animation fadeSlideUp(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getJavaFxNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getJavaFxNode());

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromY(15);    tt.setToY(0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromY(0);     tt.setToY(15);
        }

        var p = new ParallelTransition(ft, tt);
        p.setDelay(delay);
        return p;
    }

    /** Smooth scale up/down from center, combined with fade. Good for modals/cards. */
    public static Animation fadeScale(Component c, boolean entering) {
        return fadeScale(c, entering, Duration.millis(180), Duration.ZERO);
    }

    public static Animation fadeScale(Component c, boolean entering, Duration duration) {
        return fadeScale(c, entering, duration, Duration.ZERO);
    }

    public static Animation fadeScale(Component c, boolean entering, Duration duration, Duration delay) {
        FadeTransition ft = new FadeTransition(duration, c.getJavaFxNode());
        ScaleTransition st = new ScaleTransition(duration, c.getJavaFxNode());

        if (entering) {
            ft.setFromValue(0);   ft.setToValue(1);
            st.setFromX(0.92);    st.setToX(1.0);
            st.setFromY(0.92);    st.setToY(1.0);
        } else {
            ft.setFromValue(1);   ft.setToValue(0);
            st.setFromX(1.0);     st.setToX(0.92);
            st.setFromY(1.0);     st.setToY(0.92);
        }

        var p = new ParallelTransition(ft, st);
        p.setDelay(delay);
        return p;
    }

    /** Emphatic "pop" — overshoots slightly before settling. Good for drawing attention (badges, alerts). */
    public static Animation pop(Component c, boolean entering) {
        return pop(c, entering, Duration.ZERO);
    }

    public static Animation pop(Component c, boolean entering, Duration delay) {
        ScaleTransition st = new ScaleTransition(Duration.millis(220), c.getJavaFxNode());
        FadeTransition ft = new FadeTransition(Duration.millis(220), c.getJavaFxNode());

        if (entering) {
            st.setFromX(0.7); st.setToX(1.0);
            st.setFromY(0.7); st.setToY(1.0);
            ft.setFromValue(0); ft.setToValue(1);
        } else {
            st.setFromX(1.0); st.setToX(0.7);
            st.setFromY(1.0); st.setToY(0.7);
            ft.setFromValue(1); ft.setToValue(0);
        }

        var p = new ParallelTransition(ft, st);
        p.setDelay(delay);
        return p;
    }

    /** Longer horizontal slide without fade — useful for carousel/wizard screen transitions. */
    public static Animation slideHorizontal(Component c, boolean entering) {
        return slideHorizontal(c, entering, Duration.ZERO);
    }

    public static Animation slideHorizontal(Component c, boolean entering, Duration delay) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getJavaFxNode());
        if (entering) {
            tt.setFromX(40);
            tt.setToX(0);
        } else {
            tt.setFromX(0);
            tt.setToX(-40);
        }
        tt.setDelay(delay);
        return tt;
    }

    /** Horizontal shake — useful for validation errors, wrong passwords, alerts. */
    public static Animation shake(Component c) {
        return shake(c, Duration.ZERO);
    }

    public static Animation shake(Component c, Duration delay) {
        TranslateTransition t1 = new TranslateTransition(Duration.millis(50), c.getJavaFxNode());
        t1.setFromX(0); t1.setToX(-8);

        TranslateTransition t2 = new TranslateTransition(Duration.millis(50), c.getJavaFxNode());
        t2.setFromX(-8); t2.setToX(8);

        TranslateTransition t3 = new TranslateTransition(Duration.millis(50), c.getJavaFxNode());
        t3.setFromX(8); t3.setToX(-6);

        TranslateTransition t4 = new TranslateTransition(Duration.millis(50), c.getJavaFxNode());
        t4.setFromX(-6); t4.setToX(6);

        TranslateTransition t5 = new TranslateTransition(Duration.millis(50), c.getJavaFxNode());
        t5.setFromX(6); t5.setToX(0);

        var seq = new SequentialTransition(t1, t2, t3, t4, t5);
        seq.setDelay(delay);
        return seq;
    }

    /** Vertical bounce — rises and falls with easing. Good for badges, notifications. */
    public static Animation bounce(Component c) {
        return bounce(c, Duration.ZERO);
    }

    public static Animation bounce(Component c, Duration delay) {
        TranslateTransition up = new TranslateTransition(Duration.millis(200), c.getJavaFxNode());
        up.setFromY(0); up.setToY(-15);

        TranslateTransition down = new TranslateTransition(Duration.millis(200), c.getJavaFxNode());
        down.setFromY(-15); down.setToY(0);

        TranslateTransition up2 = new TranslateTransition(Duration.millis(150), c.getJavaFxNode());
        up2.setFromY(0); up2.setToY(-6);

        TranslateTransition down2 = new TranslateTransition(Duration.millis(150), c.getJavaFxNode());
        down2.setFromY(-6); down2.setToY(0);

        var seq = new SequentialTransition(up, down, up2, down2);
        seq.setDelay(delay);
        return seq;
    }

    /** Scale pulse (heartbeat) — grows and returns. Good for action buttons, like icons. */
    public static Animation pulse(Component c) {
        return pulse(c, 2, Duration.millis(150), Duration.ZERO);
    }

    public static Animation pulse(Component c, int cycles) {
        return pulse(c, cycles, Duration.millis(150), Duration.ZERO);
    }

    public static Animation pulse(Component c, Duration delay) {
        return pulse(c, 2, Duration.millis(150), delay);
    }

    public static Animation pulse(Component c, int cycles, Duration delay) {
        return pulse(c, cycles, Duration.millis(150), delay);
    }

    public static Animation pulse(Component c, int cycles, Duration speed, Duration delay) {
        ScaleTransition st = new ScaleTransition(speed, c.getJavaFxNode());
        st.setFromX(1.0); st.setToX(1.15);
        st.setFromY(1.0); st.setToY(1.15);
        st.setAutoReverse(true);
        st.setCycleCount(cycles);
        st.setDelay(delay);
        return st;
    }

    /** Opacity flash — quick blink. Good for urgent alerts, new messages. */
    public static Animation flash(Component c) {
        return flash(c, Duration.ZERO);
    }

    public static Animation flash(Component c, Duration delay) {
        FadeTransition f1 = new FadeTransition(Duration.millis(100), c.getJavaFxNode());
        f1.setFromValue(1.0); f1.setToValue(0.0);

        FadeTransition f2 = new FadeTransition(Duration.millis(100), c.getJavaFxNode());
        f2.setFromValue(0.0); f2.setToValue(1.0);

        FadeTransition f3 = new FadeTransition(Duration.millis(100), c.getJavaFxNode());
        f3.setFromValue(1.0); f3.setToValue(0.0);

        FadeTransition f4 = new FadeTransition(Duration.millis(100), c.getJavaFxNode());
        f4.setFromValue(0.0); f4.setToValue(1.0);

        var seq = new SequentialTransition(f1, f2, f3, f4);
        seq.setDelay(delay);
        return seq;
    }

    /** Wobble — lateral oscillation with fade out. Good for elements being dismissed. */
    public static Animation wobble(Component c) {
        return wobble(c, Duration.ZERO);
    }

    public static Animation wobble(Component c, Duration delay) {
        TranslateTransition t1 = new TranslateTransition(Duration.millis(100), c.getJavaFxNode());
        t1.setFromX(0); t1.setToX(-10);

        TranslateTransition t2 = new TranslateTransition(Duration.millis(100), c.getJavaFxNode());
        t2.setFromX(-10); t2.setToX(10);

        TranslateTransition t3 = new TranslateTransition(Duration.millis(100), c.getJavaFxNode());
        t3.setFromX(10); t3.setToX(-5);

        TranslateTransition t4 = new TranslateTransition(Duration.millis(100), c.getJavaFxNode());
        t4.setFromX(-5); t4.setToX(5);

        TranslateTransition t5 = new TranslateTransition(Duration.millis(100), c.getJavaFxNode());
        t5.setFromX(5); t5.setToX(0);

        FadeTransition fade = new FadeTransition(Duration.millis(400), c.getJavaFxNode());
        fade.setFromValue(1.0); fade.setToValue(0.0);

        var seq = new SequentialTransition(
                new ParallelTransition(t1),
                new ParallelTransition(t2),
                new ParallelTransition(t3),
                new ParallelTransition(t4),
                new ParallelTransition(t5),
                fade
        );
        seq.setDelay(delay);
        return seq;
    }

    /** Slide in from the left — useful for side menus, drawers. */
    public static Animation slideInLeft(Component c, boolean entering) {
        return slideInLeft(c, entering, Duration.ZERO);
    }

    public static Animation slideInLeft(Component c, boolean entering, Duration delay) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getJavaFxNode());
        FadeTransition ft = new FadeTransition(Duration.millis(250), c.getJavaFxNode());

        if (entering) {
            tt.setFromX(-60); tt.setToX(0);
            ft.setFromValue(0); ft.setToValue(1);
        } else {
            tt.setFromX(0); tt.setToX(-60);
            ft.setFromValue(1); ft.setToValue(0);
        }

        var p = new ParallelTransition(tt, ft);
        p.setDelay(delay);
        return p;
    }

    /** Slide in from the right — useful for detail panels, sidebar. */
    public static Animation slideInRight(Component c, boolean entering) {
        return slideInRight(c, entering, Duration.ZERO);
    }

    public static Animation slideInRight(Component c, boolean entering, Duration delay) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getJavaFxNode());
        FadeTransition ft = new FadeTransition(Duration.millis(250), c.getJavaFxNode());

        if (entering) {
            tt.setFromX(60); tt.setToX(0);
            ft.setFromValue(0); ft.setToValue(1);
        } else {
            tt.setFromX(0); tt.setToX(60);
            ft.setFromValue(1); ft.setToValue(0);
        }

        var p = new ParallelTransition(tt, ft);
        p.setDelay(delay);
        return p;
    }

    /**
     * Gentle entrance for card grids: fade + slight rise + slight scale, with
     * EASE_OUT easing and longer duration (450ms by default) — designed to not
     * feel like a harsh "pop".
     */
    public static Animation riseIn(Component c, boolean entering) {
        return riseIn(c, entering, Duration.millis(450));
    }

    public static Animation riseIn(Component c, boolean entering, Duration duration) {
        return riseIn(c, entering, duration, Duration.ZERO);
    }

    public static Animation riseIn(Component c, boolean entering, Duration duration, Duration delay) {
        FadeTransition ft = new FadeTransition(duration, c.getJavaFxNode());
        TranslateTransition tt = new TranslateTransition(duration, c.getJavaFxNode());
        ScaleTransition st = new ScaleTransition(duration, c.getJavaFxNode());

        ft.setInterpolator(Interpolator.EASE_OUT);
        tt.setInterpolator(Interpolator.EASE_OUT);
        st.setInterpolator(Interpolator.EASE_OUT);

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromY(18);    tt.setToY(0);
            st.setFromX(0.96);  st.setToX(1.0);
            st.setFromY(0.96);  st.setToY(1.0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromY(0);     tt.setToY(18);
            st.setFromX(1.0);   st.setToX(0.96);
            st.setFromY(1.0);   st.setToY(0.96);
        }

        var p = new ParallelTransition(ft, tt, st);
        p.setDelay(delay);
        return p;
    }
}
