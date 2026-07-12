package megalodonte.base;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import megalodonte.base.components.Component;

public class Animations {

    public static Animation fadeSlide(Component c, boolean entering) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getNode());

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromX(20);    tt.setToX(0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromX(0);     tt.setToX(20);
        }

        return new ParallelTransition(ft, tt);
    }

    /** Fade puro, sem deslocamento. Bom para trocas de conteúdo neutras (texto, ícone). */
    public static Animation fade(Component c, boolean entering) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), c.getNode());
        if (entering) {
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
        }
        return ft;
    }

    /** Desliza de/para cima — útil para dropdowns, menus, notificações. */
    public static Animation fadeSlideDown(Component c, boolean entering) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getNode());

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromY(-15);   tt.setToY(0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromY(0);     tt.setToY(-15);
        }

        return new ParallelTransition(ft, tt);
    }

    /** Desliza de/para baixo — útil para toasts/popups que sobem da parte inferior da tela. */
    public static Animation fadeSlideUp(Component c, boolean entering) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), c.getNode());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), c.getNode());

        if (entering) {
            ft.setFromValue(0); ft.setToValue(1);
            tt.setFromY(15);    tt.setToY(0);
        } else {
            ft.setFromValue(1); ft.setToValue(0);
            tt.setFromY(0);     tt.setToY(15);
        }

        return new ParallelTransition(ft, tt);
    }

    /** Cresce/encolhe suavemente a partir do centro, combinado com fade. Bom para modais/cards. */
    public static Animation fadeScale(Component c, boolean entering) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), c.getNode());
        ScaleTransition st = new ScaleTransition(Duration.millis(180), c.getNode());

        if (entering) {
            ft.setFromValue(0);   ft.setToValue(1);
            st.setFromX(0.92);    st.setToX(1.0);
            st.setFromY(0.92);    st.setToY(1.0);
        } else {
            ft.setFromValue(1);   ft.setToValue(0);
            st.setFromX(1.0);     st.setToX(0.92);
            st.setFromY(1.0);     st.setToY(0.92);
        }

        return new ParallelTransition(ft, st);
    }

    /** "Pop" mais enfático — passa de um pouco maior que o normal antes de assentar. Bom para chamar atenção (badges, alertas). */
    public static Animation pop(Component c, boolean entering) {
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

        return new ParallelTransition(ft, st);
    }

    /** Slide horizontal mais longo, sem fade — útil para trocas de tela tipo carrossel/wizard. */
    public static Animation slideHorizontal(Component c, boolean entering) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), c.getNode());
        if (entering) {
            tt.setFromX(40);
            tt.setToX(0);
        } else {
            tt.setFromX(0);
            tt.setToX(-40);
        }
        return tt;
    }
}