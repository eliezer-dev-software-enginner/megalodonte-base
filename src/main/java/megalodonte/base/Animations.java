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
    /** Rotação livre entre dois ângulos. Útil para ícones de refresh, toggles, setas de expand/collapse. */
    public static Animation rotate(Component c, double fromAngle, double toAngle, Duration duration) {
        javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(duration, c.getNode());
        rt.setFromAngle(fromAngle);
        rt.setToAngle(toAngle);
        return rt;
    }

    /** Giro completo de 360°. Bom para ícone de "atualizar"/loading ao clicar. */
    public static Animation rotate360(Component c) {
        return rotate(c, 0, 360, Duration.millis(500));
    }
    public static Animation fadeSlide(Component c, boolean entering) {
        return fadeSlide(c, entering, Duration.ZERO);
    }

    public static Animation fadeSlide(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getNode());

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

    /** Fade puro, sem deslocamento. Bom para trocas de conteúdo neutras (texto, ícone). */
    public static Animation fade(Component c, boolean entering) {
        return fade(c, entering, Duration.ZERO);
    }

    public static Animation fade(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), c.getNode());
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

    /** Desliza de/para cima — útil para dropdowns, menus, notificações. */
    public static Animation fadeSlideDown(Component c, boolean entering) {
        return fadeSlideDown(c, entering, Duration.ZERO);
    }

    public static Animation fadeSlideDown(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getNode());

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

    /** Desliza de/para baixo — útil para toasts/popups que sobem da parte inferior da tela. */
    public static Animation fadeSlideUp(Component c, boolean entering) {
        return fadeSlideUp(c, entering, Duration.ZERO);
    }

    public static Animation fadeSlideUp(Component c, boolean entering, Duration delay) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getNode());

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

    /** Cresce/encolhe suavemente a partir do centro, combinado com fade. Bom para modais/cards. */
    public static Animation fadeScale(Component c, boolean entering) {
        return fadeScale(c, entering, Duration.millis(180), Duration.ZERO);
    }

    public static Animation fadeScale(Component c, boolean entering, Duration duration) {
        return fadeScale(c, entering, duration, Duration.ZERO);
    }

    public static Animation fadeScale(Component c, boolean entering, Duration duration, Duration delay) {
        FadeTransition ft = new FadeTransition(duration, c.getNode());
        ScaleTransition st = new ScaleTransition(duration, c.getNode());

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

    /** "Pop" mais enfático — passa de um pouco maior que o normal antes de assentar. Bom para chamar atenção (badges, alertas). */
    public static Animation pop(Component c, boolean entering) {
        return pop(c, entering, Duration.ZERO);
    }

    public static Animation pop(Component c, boolean entering, Duration delay) {
        ScaleTransition st = new ScaleTransition(Duration.millis(220), c.getNode());
        FadeTransition ft = new FadeTransition(Duration.millis(220), c.getNode());

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

    /** Slide horizontal mais longo, sem fade — útil para trocas de tela tipo carrossel/wizard. */
    public static Animation slideHorizontal(Component c, boolean entering) {
        return slideHorizontal(c, entering, Duration.ZERO);
    }

    public static Animation slideHorizontal(Component c, boolean entering, Duration delay) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getNode());
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

    /** Tremor horizontal — útil para erros de validação, senhas erradas, alerts. */
    public static Animation shake(Component c) {
        return shake(c, Duration.ZERO);
    }

    public static Animation shake(Component c, Duration delay) {
        TranslateTransition t1 = new TranslateTransition(Duration.millis(50), c.getNode());
        t1.setFromX(0); t1.setToX(-8);

        TranslateTransition t2 = new TranslateTransition(Duration.millis(50), c.getNode());
        t2.setFromX(-8); t2.setToX(8);

        TranslateTransition t3 = new TranslateTransition(Duration.millis(50), c.getNode());
        t3.setFromX(8); t3.setToX(-6);

        TranslateTransition t4 = new TranslateTransition(Duration.millis(50), c.getNode());
        t4.setFromX(-6); t4.setToX(6);

        TranslateTransition t5 = new TranslateTransition(Duration.millis(50), c.getNode());
        t5.setFromX(6); t5.setToX(0);

        var seq = new SequentialTransition(t1, t2, t3, t4, t5);
        seq.setDelay(delay);
        return seq;
    }

    /** Bounce vertical — sobe e cai com easing. Bom para badges, notificações. */
    public static Animation bounce(Component c) {
        return bounce(c, Duration.ZERO);
    }

    public static Animation bounce(Component c, Duration delay) {
        TranslateTransition up = new TranslateTransition(Duration.millis(200), c.getNode());
        up.setFromY(0); up.setToY(-15);

        TranslateTransition down = new TranslateTransition(Duration.millis(200), c.getNode());
        down.setFromY(-15); down.setToY(0);

        TranslateTransition up2 = new TranslateTransition(Duration.millis(150), c.getNode());
        up2.setFromY(0); up2.setToY(-6);

        TranslateTransition down2 = new TranslateTransition(Duration.millis(150), c.getNode());
        down2.setFromY(-6); down2.setToY(0);

        var seq = new SequentialTransition(up, down, up2, down2);
        seq.setDelay(delay);
        return seq;
    }

    /** Pulso de escala (heartbeat) — cresce e volta. Bom para botões de ação, ícones de like. */
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
        ScaleTransition st = new ScaleTransition(speed, c.getNode());
        st.setFromX(1.0); st.setToX(1.15);
        st.setFromY(1.0); st.setToY(1.15);
        st.setAutoReverse(true);
        st.setCycleCount(cycles);
        st.setDelay(delay);
        return st;
    }

    /** Flash de opacidade — pisca rápido. Bom para alertas urgentes, novas mensagens. */
    public static Animation flash(Component c) {
        return flash(c, Duration.ZERO);
    }

    public static Animation flash(Component c, Duration delay) {
        FadeTransition f1 = new FadeTransition(Duration.millis(100), c.getNode());
        f1.setFromValue(1.0); f1.setToValue(0.0);

        FadeTransition f2 = new FadeTransition(Duration.millis(100), c.getNode());
        f2.setFromValue(0.0); f2.setToValue(1.0);

        FadeTransition f3 = new FadeTransition(Duration.millis(100), c.getNode());
        f3.setFromValue(1.0); f3.setToValue(0.0);

        FadeTransition f4 = new FadeTransition(Duration.millis(100), c.getNode());
        f4.setFromValue(0.0); f4.setToValue(1.0);

        var seq = new SequentialTransition(f1, f2, f3, f4);
        seq.setDelay(delay);
        return seq;
    }

    /** Wobble — oscilação lateral com fade out. Bom para elementos sendo descartados. */
    public static Animation wobble(Component c) {
        return wobble(c, Duration.ZERO);
    }

    public static Animation wobble(Component c, Duration delay) {
        TranslateTransition t1 = new TranslateTransition(Duration.millis(100), c.getNode());
        t1.setFromX(0); t1.setToX(-10);

        TranslateTransition t2 = new TranslateTransition(Duration.millis(100), c.getNode());
        t2.setFromX(-10); t2.setToX(10);

        TranslateTransition t3 = new TranslateTransition(Duration.millis(100), c.getNode());
        t3.setFromX(10); t3.setToX(-5);

        TranslateTransition t4 = new TranslateTransition(Duration.millis(100), c.getNode());
        t4.setFromX(-5); t4.setToX(5);

        TranslateTransition t5 = new TranslateTransition(Duration.millis(100), c.getNode());
        t5.setFromX(5); t5.setToX(0);

        FadeTransition fade = new FadeTransition(Duration.millis(400), c.getNode());
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

    /** Slide in da esquerda — útil para menus laterais, drawers. */
    public static Animation slideInLeft(Component c, boolean entering) {
        return slideInLeft(c, entering, Duration.ZERO);
    }

    public static Animation slideInLeft(Component c, boolean entering, Duration delay) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getNode());
        FadeTransition ft = new FadeTransition(Duration.millis(250), c.getNode());

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

    /** Slide in da direita — útil para painéis de detalhe, sidebar. */
    public static Animation slideInRight(Component c, boolean entering) {
        return slideInRight(c, entering, Duration.ZERO);
    }

    public static Animation slideInRight(Component c, boolean entering, Duration delay) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getNode());
        FadeTransition ft = new FadeTransition(Duration.millis(250), c.getNode());

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
     * Entrada suave para grids de cards: fade + leve subida + leve escala, com
     * easing (EASE_OUT) e duração mais longa (450ms por padrão) que as demais
     * combinações fade+slide daqui — pensada pra não parecer um "pop" seco.
     */
    public static Animation riseIn(Component c, boolean entering) {
        return riseIn(c, entering, Duration.millis(450));
    }

    public static Animation riseIn(Component c, boolean entering, Duration duration) {
        return riseIn(c, entering, duration, Duration.ZERO);
    }

    public static Animation riseIn(Component c, boolean entering, Duration duration, Duration delay) {
        FadeTransition ft = new FadeTransition(duration, c.getNode());
        TranslateTransition tt = new TranslateTransition(duration, c.getNode());
        ScaleTransition st = new ScaleTransition(duration, c.getNode());

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
