package megalodonte.base;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
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
}
