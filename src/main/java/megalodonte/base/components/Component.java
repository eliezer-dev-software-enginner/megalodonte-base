package megalodonte.base.components;

import javafx.scene.Node;

/**
 * Base class for all UI components. Wraps a JavaFX {@link Node} and provides
 * a React-inspired API for building component trees.
 * <p>
 * Subclasses override {@link #getJavaFxNode()} to return the underlying node.
 * Use {@link #CreateFromJavaFxNode} to wrap an existing node as a component.
 */
public abstract class Component implements ComponentInterface<Component> {
    protected final Node node;
    public PropsInterface props;

    protected Component(Node node) {
        this.node = node;
    }

    protected Component(Node node, PropsInterface props) {
        this.node = node;
        setProps(node, props);
    }

    private void setProps(Node node, PropsInterface props) {
        if(props != null){
            this.props = props;
            this.props.apply(node);
        }
    }

    @Override
    public Node getJavaFxNode() {
        return node;
    }

    @Override
    public Component fromJavaFxNode(Node newNode) {
        return CreateFromJavaFxNode(newNode);
    }

    /**
     * Factory method to create a wrapper {@link Component} around an existing JavaFX Node.
     *
     * @param node the JavaFX node to wrap (must not be null)
     * @return a new component wrapping the given node
     * @throws IllegalArgumentException if node is null
     */
    public static Component CreateFromJavaFxNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node can not be null");
        }

        // Wrapper component for an existing JavaFX Node
        return new Component(node) {
            @Override
            public Node getJavaFxNode() {
                return node; // Returns the original node
            }

            @Override
            public Component fromJavaFxNode(Node newNode) {
                return CreateFromJavaFxNode(newNode); // Delegates to the static method
            }
        };
    }

    /**
     * Binds this component to a {@link Ref}, making it accessible from
     * the parent scope.
     *
     * @param ref the reference to bind to
     * @return this component for chaining
     */
    public <T extends Component> Component ref(Ref<T> ref) {
        ref.setCurrent((T) this);
        return this;
    }

    @FunctionalInterface
    public interface Transition {
        javafx.animation.Animation play(Component c, boolean entering);
    }

}