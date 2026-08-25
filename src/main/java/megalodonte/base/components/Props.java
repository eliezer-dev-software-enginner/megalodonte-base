package megalodonte.base.components;

import javafx.scene.Node;
import megalodonte.base.state.ReadableState;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;

import java.util.function.Consumer;

/**
 * Abstract base for reactive, theme-aware component properties. Handles:
 * <ul>
 *   <li>State binding via {@link #bind}</li>
 *   <li>Automatic theme re-application when {@link ThemeManager} changes</li>
 * </ul>
 * Subclasses implement {@link #applyTheme} to define how the theme maps to
 * node styles.
 */
public abstract class Props implements PropsInterface {

    /** Binds this property set to the node and subscribes to theme changes. */
    final public void apply(Node node) {
        bindStates(node);

        ThemeManager.state().subscribe(theme -> {
            if (theme == null) return;
            applyTheme(node, this, theme);
        });
    }

    /** Override to perform initial state bindings. Called once during {@link #apply}. */
    protected void bindStates(Node node) {}

    /**
     * Subscribes a state to a handler. The handler is called immediately with
     * the current value and again on every subsequent change.
     *
     * @param node    the node to style
     * @param state   the state to observe
     * @param handler called with each new value
     */
    protected <T> void bind(Node node, ReadableState<T> state, Consumer<T> handler) {
        if (state != null) state.subscribe(handler);
    }

    protected abstract void applyTheme(Node node, Props props, ThemeInterface theme);
}
