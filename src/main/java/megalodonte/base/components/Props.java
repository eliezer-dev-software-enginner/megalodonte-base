package megalodonte.base.components;

import javafx.scene.Node;
import megalodonte.base.state.ReadableState;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;

import java.util.function.Consumer;

public abstract class Props implements PropsInterface {

    final public void apply(Node node) {
        bindStates(node);

        ThemeManager.state().subscribe(theme -> {
            if (theme == null) return;
            applyTheme(node, this, theme);
        });
    }

    protected void bindStates(Node node) {}

    protected <T> void bind(Node node, ReadableState<T> state, Consumer<T> handler) {
        if (state != null) state.subscribe(handler);
    }

    protected abstract void applyTheme(Node node, Props props, ThemeInterface theme);
}
