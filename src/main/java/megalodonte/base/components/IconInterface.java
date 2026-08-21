package megalodonte.base.components;

import javafx.scene.Node;

public abstract class IconInterface {
    public abstract Node getJavaFxNode();

    public IconInterface ref(Ref<IconInterface> ref) {
        ref.setCurrent(this);
        return this;
    }

    public static IconInterface of(Node node) {
        return new IconInterface() {
            @Override
            public Node getJavaFxNode() {
                return node;
            }
        };
    }
}