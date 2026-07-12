package megalodonte.application;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import megalodonte.base.components.ComponentInterface;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.RouteResult;
import megalodonte.base.route.RouterBase;
import megalodonte.base.scale.ScaleProvider;


public final class Context {

    private final Stage stage;
    private final String[] args;
    public final double width = 600, height = 500;

    Context(Stage stage, String[] args) {
        this.stage = stage;
        this.args = args == null ? new String[0] : args;
    }

    Context(Stage stage) {
        this(stage, new String[0]);
    }

    public String[] getArgs() {
        return args;
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

    public void useView(RouteResult routeResult) {
        var props = routeResult.props();
        var parentLayout = (Parent) routeResult.view().getJavaFxNode();
        stage.setResizable(props.screenIsExpandable());
        stage.setScene(new Scene(parentLayout,
                ScaleProvider.scale(props.screenWidth()),
                ScaleProvider.scale(props.screenHeight())));
        stage.setTitle(routeResult.props().name());
        if (props.iconPath() != null && !props.iconPath().isEmpty()) {
            stage.getIcons().add(new Image(props.iconPath()));
        }
        stage.centerOnScreen();
        // onMount já foi chamado dentro do Router.resolveWithStage()
    }

    /**
     * Advise: onMount will not work using this method
     * @param component
     */
    @Deprecated(forRemoval = true)
    public void useView(ComponentInterface<?> component) {
        stage.setScene(new Scene((Parent) component.getJavaFxNode(), width, height));
    }

    public RouterBuilder useRouter(RouterBase router) {
        router.bind(this);
        return new RouterBuilder(router);
    }

    public final class RouterBuilder {
        private final RouterBase router;

        RouterBuilder(RouterBase router) {
            this.router = router;
        }

        public void start() {
            useView(router.entrypoint());
        }
    }
}
