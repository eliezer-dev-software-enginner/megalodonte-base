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
import megalodonte.base.theme.ThemeManager;


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
        var scene = new Scene(parentLayout, width, height);
        ThemeManager.applyFontFamily(scene);
        stage.setScene(scene);

        // Ambos rodam depois do stage.show() do Bootstrap (que só acontece depois
        // deste handler retornar). onMount precisa do stage já visível e da Scene já
        // anexada. centerOnScreen() precisa do tamanho FINAL da stage — é o show()/
        // layout inicial que aplica minWidth/minHeight de fato, se a app tiver
        // setado; chamado antes disso ele centralizaria contra um tamanho ainda
        // provisório, e a janela "cresceria" pra direita/baixo depois de já
        // posicionada (bug real observado numa app que setava minWidth/minHeight
        // antes de useView()). runLater roda no próximo pulse, já com show() concluído.
        Platform.runLater(() -> {
            component.onMount();
            stage.centerOnScreen();
        });
    }

    public void useView(RouteResult routeResult) {
        var props = routeResult.props();
        var parentLayout = (Parent) routeResult.view().getJavaFxNode();
        stage.setResizable(props.screenIsExpandable());
        var scene = new Scene(parentLayout,
                ScaleProvider.scale(props.screenWidth()),
                ScaleProvider.scale(props.screenHeight()));
        ThemeManager.applyFontFamily(scene);
        stage.setScene(scene);
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
        var scene = new Scene((Parent) component.getJavaFxNode(), width, height);
        ThemeManager.applyFontFamily(scene);
        stage.setScene(scene);
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
