package megalodonte.base.components;

import javafx.animation.Animation;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.function.Function;

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

    private Function<Component, Animation> animationHandler;

    public Component attachAnimation(Function<Component, Animation> animationHandler){
        this.animationHandler = animationHandler;
        if (this.animationHandler == null) return this;

        if (node.getScene() != null) {
            playAnimation();
        } else {
            node.sceneProperty().addListener(new javafx.beans.value.ChangeListener<Scene>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends Scene> obs, Scene oldScene, Scene newScene) {
                    if (newScene != null) {
                        playAnimation();
                        node.sceneProperty().removeListener(this);
                    }
                }
            });
        }

        return this;
    }

    private void playAnimation() {
        Animation animation = this.animationHandler.apply(this);
        if (animation != null) {
            animation.play();
        }
    }




    @FunctionalInterface
    public interface Transition {
        Animation play(Component c, boolean entering);
    }

}