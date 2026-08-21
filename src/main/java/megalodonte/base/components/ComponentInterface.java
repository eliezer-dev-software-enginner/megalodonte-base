package megalodonte.base.components;

import javafx.scene.Node;

public interface ComponentInterface <T extends ComponentInterface<T>> {
    Node getJavaFxNode();
    T fromJavaFxNode(Node node);
}
