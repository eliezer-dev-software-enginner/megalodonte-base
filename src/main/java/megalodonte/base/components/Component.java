package megalodonte.base.components;

import javafx.scene.Node;

public abstract class Component implements ComponentInterface<Component> {
    protected final Node node;
    public PropsInterface props;

    public Node getNode() {
        return node;
    }

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
     * Factory method estático para criar um Component a partir de um Node JavaFX existente.
     * Este método segue o padrão Factory e cria um component wrapper.
     */
    public static Component CreateFromJavaFxNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node can not be null");
        }

        // Wrapper component para um Node JavaFX existente
        return new Component(node) {
            @Override
            public Node getJavaFxNode() {
                return node; // Retorna o node original
            }

            @Override
            public Component fromJavaFxNode(Node newNode) {
                return CreateFromJavaFxNode(newNode); // Delega para o método estático
            }
        };
    }

    public <T extends Component> Component ref(Ref<T> ref) {
        ref.setCurrent((T) this);
        return this;
    }

    @FunctionalInterface
    public interface Transition {
        javafx.animation.Animation play(Component c, boolean entering);
    }

}