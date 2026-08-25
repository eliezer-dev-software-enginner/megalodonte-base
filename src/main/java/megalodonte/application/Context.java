package megalodonte.application;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
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

        // Runs after Bootstrap's stage.show(). onMount executes with the stage already
        // visible and the Scene already attached.
        Platform.runLater(component::onMount);
        centerAfterLayoutSettles(stage);
    }

    /**
     * Re-renders {@code component} in place — swaps the current {@link Scene}'s
     * root node, keeping the Stage's existing Scene, size and position
     * untouched. Unlike {@link #useView(ScreenComponent)}, which creates a
     * brand-new {@code Scene} and re-centers the window (right for the
     * <em>first</em> render, since the Stage doesn't have a size to preserve
     * yet), this is for reactive re-renders of an already-showing screen —
     * e.g. switching themes — where the window visibly jumping/resizing
     * would be a regression, not a feature.
     * <p>
     * Deliberately does NOT call {@code component.onMount()} — that hook is a
     * one-time "just mounted" signal (e.g. {@code HomeScreen.onMount()} reopens
     * the last saved layout from disk), not something that should re-fire on
     * every re-render triggered by this method.
     * <p>
     * Falls back to {@link #useView(ScreenComponent)} if the Stage has no
     * Scene yet (nothing to swap the root of).
     */
    public void updateView(ScreenComponent component) {
        Scene scene = stage.getScene();
        if (scene == null) {
            useView(component);
            return;
        }
        var parentLayout = (Parent) component.render().getJavaFxNode();
        scene.setRoot(parentLayout);
    }

    /**
     * centerOnScreen() precisa do tamanho FINAL da stage, mas quanto tempo o layout
     * leva pra se estabilizar varia — fontes/imagens carregando, várias passadas de
     * CSS/layout, minWidth/minHeight só aplicados no show() do Bootstrap (que só
     * acontece depois do handler de {@code MegalodonteApp.run(...)} retornar). Um
     * único {@code Platform.runLater(...)} funciona às vezes e às vezes não —
     * observado na prática: a janela abria centralizada em algumas execuções e
     * deslocada em outras, mesmo sem mudar nada no código.
     * <p>
     * Em vez de adivinhar quantos pulses esperar, reage a QUALQUER mudança real de
     * largura/altura da stage recentralizando, e para de escutar depois de uma janela
     * curta — do contrário, mudanças de tamanho legítimas e posteriores (o usuário
     * redimensionando a janela, ou uma tela trocando o conteúdo depois) forçariam a
     * app a se recentralizar sozinha pra sempre, o que seria um bug por si só.
     */
    private static void centerAfterLayoutSettles(Stage stage) {
        ChangeListener<Number> recenter = (obs, oldValue, newValue) -> stage.centerOnScreen();
        stage.widthProperty().addListener(recenter);
        stage.heightProperty().addListener(recenter);

        PauseTransition settle = new PauseTransition(Duration.millis(300));
        settle.setOnFinished(e -> {
            stage.widthProperty().removeListener(recenter);
            stage.heightProperty().removeListener(recenter);
            stage.centerOnScreen();
        });
        settle.play();
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
        // onMount was already called inside Router.resolveWithStage()
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
