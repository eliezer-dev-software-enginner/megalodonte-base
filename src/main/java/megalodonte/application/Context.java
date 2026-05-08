package megalodonte.application;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import megalodonte.base.components.ComponentInterface;
import megalodonte.base.components.ScreenComponent;
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

    public void useView(Class<? extends  ScreenComponent> componentClass) {
        try {
            ScreenComponent component = componentClass.getDeclaredConstructor().newInstance();
            useView(component);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate: " + componentClass.getName(), e);
        }
    }

    public void useView(ScreenComponent component) {
       var parentLayout = (Parent) component.render().getJavaFxNode();
        stage.setScene(new Scene(parentLayout, width, height));

        //após o stage.show() do Bootstrap executar. O onMount vai rodar com o stage já visível e a Scene já anexada.
        Platform.runLater(component::onMount);
    }

    /**
     * Advise: onMount will not work using this method
     * @param component
     */
    @Deprecated(forRemoval = true)
    public void useView(ComponentInterface<?> component) {
        stage.setScene(new Scene((Parent) component.getJavaFxNode(), width, height));
    }

    public RouterBase useRouter(RouterBase router) {
        router.bind(this);
        return router;
    }
}
