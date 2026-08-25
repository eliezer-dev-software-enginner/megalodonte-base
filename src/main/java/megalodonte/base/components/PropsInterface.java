package megalodonte.base.components;

import javafx.scene.Node;

/**
 * Contract for component property sets. Applies styles, bindings, and
 * theme rules to a JavaFX {@link Node}.
 */
public interface PropsInterface {
    /** Applies this property set to the given node. */
    void apply(Node node);
}
