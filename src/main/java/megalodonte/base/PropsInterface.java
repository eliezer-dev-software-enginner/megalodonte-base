package megalodonte.base;

import javafx.scene.Node;
import megalodonte.base.theme.ThemeInterface;

public interface PropsInterface {
    //public abstract void apply(Node node);

    /**
     * Template method that handles theme subscription and calls applyTheme.
     * Subclasses should implement applyTheme for their specific styling logic.
     */
    void apply(Node node);

     void applyTheme(Node node, PropsInterface props, ThemeInterface theme);
}
