package megalodonte.application;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import megalodonte.base.components.ComponentInterface;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.RouteResult;
import megalodonte.base.route.RouterBase;
import megalodonte.base.scale.ScaleProvider;
import megalodonte.base.theme.ThemeManager;


/**
 * Application context available to the startup handler. Provides access to the
 * primary {@link Stage}, command-line arguments, and view/navigation methods.
 * <p>
 * Created once by {@link Bootstrap#dispatch} and passed to the application handler
 * via {@link MegalodonteApp#run}.
 */
public final class Context {
    public megalodonte.contracts.BackendContract backendContract() { return megalodonte.platform.javafx.JavaFxPlatform.contract(); }

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

    /** Returns the command-line arguments passed to the application. @return the arguments array */
    public String[] getArgs() {
        return args;
    }

    /** Returns the primary JavaFX stage managed by this context. @return the primary stage */
    public Stage javafxStage() {
        return stage;
    }

    /**
     * Instantiates a {@link ScreenComponent} by class and renders it as the current view.
     * Creates a new {@link Scene} with the default dimensions and centers the stage.
     *
     * @param componentClass the screen component class to instantiate
     * @throws RuntimeException if instantiation fails
     */
    public void useView(Class<? extends  ScreenComponent> componentClass) {
        try {
            ScreenComponent component = componentClass.getDeclaredConstructor().newInstance();
            useView(component);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate: " + componentClass.getName(), e);
        }
    }

    /**
     * Renders a {@link ScreenComponent} as the current view. Creates a new {@link Scene},
     * applies the theme font, sets it on the stage, and schedules {@code onMount}.
     *
     * @param component the screen component to render
     */
    public void useView(ScreenComponent component) {
        useView(component, width, height);
    }

    /** Creates the first scene at its intended size, avoiding native resize feedback at startup. */
    public void useView(ScreenComponent component, double sceneWidth, double sceneHeight) {
        if (!Double.isFinite(sceneWidth) || !Double.isFinite(sceneHeight) || sceneWidth <= 0 || sceneHeight <= 0)
            throw new IllegalArgumentException("Scene dimensions must be finite and positive");
       var parentLayout = (Parent) component.render().getJavaFxNode();
        var scene = new Scene(parentLayout, sceneWidth, sceneHeight);
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
     *
     * @param component the screen component to re-render in place
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

    /** Centers once after initial layout without reacting inside native resize events. */
    private static void centerAfterLayoutSettles(Stage stage) {
        // Set the initial position before the native window is mapped. Calling
        // centerOnScreen during GTK configure events (even in a later pulse)
        // can cause an endless configure/move loop on some window managers.
        if (!stage.isShowing() && stage.getScene() != null) {
            var bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX() + Math.max(0, (bounds.getWidth() - stage.getScene().getWidth()) / 2));
            stage.setY(bounds.getMinY() + Math.max(0, (bounds.getHeight() - stage.getScene().getHeight()) / 2));
        }
    }

    /**
     * Renders a route result, applying its {@link megalodonte.base.route.RouteProps} (dimensions, title, icon,
     * resizability) to the stage. Does not call {@code onMount} — that is handled by
     * the router.
     *
     * @param routeResult the resolved route to display
     */
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
        Bootstrap.applyStageIcon(stage, props.iconPath());
        stage.centerOnScreen();
        // onMount was already called inside Router.resolveWithStage()
    }

    /**
     * Advise: onMount will not work using this method
     * @param component the component to render (deprecated, use {@link #useView(ScreenComponent)})
     */
    @Deprecated(forRemoval = true)
    public void useView(ComponentInterface<?> component) {
        var scene = new Scene((Parent) component.getJavaFxNode(), width, height);
        ThemeManager.applyFontFamily(scene);
        stage.setScene(scene);
    }

    /**
     * Binds a router to this context and returns a {@link RouterBuilder} to start navigation.
     *
     * @param router the router to bind
     * @return a builder to call {@link RouterBuilder#start()} on
     */
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
