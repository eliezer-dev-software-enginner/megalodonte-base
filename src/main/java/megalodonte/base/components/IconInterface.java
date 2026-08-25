package megalodonte.base.components;

import javafx.scene.Node;

/**
 * Base class for icon components. Wraps a JavaFX {@link Node} and provides
 * a {@link Ref} binding API similar to {@link Component}.
 */
public abstract class IconInterface {
    /** Returns the underlying JavaFX node for this icon. */
    public abstract Node getJavaFxNode();

    /**
     * Binds this icon to a {@link Ref}, making it accessible from the parent scope.
     *
     * @param ref the reference to bind to
     * @return this icon for chaining
     */
    public IconInterface ref(Ref<IconInterface> ref) {
        ref.setCurrent(this);
        return this;
    }

    /**
     * Creates an {@link IconInterface} wrapping the given JavaFX node.
     *
     * @param node the node to wrap
     * @return a new icon instance
     */
    public static IconInterface of(Node node) {
        return new IconInterface() {
            @Override
            public Node getJavaFxNode() {
                return node;
            }
        };
    }
}