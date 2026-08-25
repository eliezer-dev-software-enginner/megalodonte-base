package megalodonte.base.components;

import javafx.scene.Node;

/**
 * Contract for components that wrap a JavaFX {@link Node}. Provides bidirectional
 * conversion between the component abstraction and the underlying node.
 *
 * @param <T> the concrete component type (self-referencing)
 */
public interface ComponentInterface <T extends ComponentInterface<T>> {
    /** Returns the underlying JavaFX node. @return the JavaFX node */
    Node getJavaFxNode();

    /** Creates a new component wrapping the given node.
     * @param node the JavaFX node to wrap
     * @return a new component wrapping the node
     */
    T fromJavaFxNode(Node node);
}
