package megalodonte.base.components;

import javafx.scene.Node;

public interface ComponentInterface <T extends ComponentInterface<T>> {
    Node getNode();
    Node getJavaFxNode();
    T fromJavaFxNode(Node node);
}
