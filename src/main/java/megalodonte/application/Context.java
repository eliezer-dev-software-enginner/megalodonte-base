package megalodonte.application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import megalodonte.base.components.ComponentInterface;
import megalodonte.base.route.RouterBase;

public final class Context {

    private final Stage stage;
    public final double width = 600, height = 500;

    Context(Stage stage) {
        this.stage = stage;
    }

    public Stage javafxStage() {
        return stage;
    }

    public void useView(ComponentInterface<?> component) {
        stage.setScene(new Scene((Parent) component.getJavaFxNode(), width, height));
    }

    public RouterBase useRouter(RouterBase router) {
        router.bind(this);
        return router;
    }
}
