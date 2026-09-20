package megalodonte.base.route;

import javafx.stage.Stage;
import megalodonte.application.ErrorReporter;
import megalodonte.base.components.ComponentInterface;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextBase;
import megalodonte.base.route.v2.ScreenContextInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen lifecycle manager owned by megalodonte-base. Tracks the active screen
 * per stage and the spawned windows, owning the shared mount/destroy logic so
 * that both the router (navigation) and {@link ScreenContextBase} (window
 * spawning) tear down screens the same way (scope cancel + onDestroy).
 */
public final class ScreenManager {
    private static final Logger log = LoggerFactory.getLogger(ScreenManager.class);

    private record ActiveScreen(ScreenComponent screen, ScreenContextInterface ctx) {}

    private final RouteTable table;
    private final Map<Stage, ActiveScreen> activeScreens = new HashMap<>();
    private final List<Stage> spawnedWindows = new ArrayList<>();

    public ScreenManager(RouteTable table) {
        this.table = table;
    }

    public RouteTable.ResolvedRoute resolve(String path) {
        return table.resolve(path);
    }

    /**
     * Mounts a screen on the given stage, destroying any previously active
     * screen on that stage. Calls {@code onMount()} before returning.
     *
     * @param stage  the stage to host the screen (may be a brand-new spawned window)
     * @return the mounted view and its route props
     */
    public RouteResult mount(Stage stage, Route route, Map<String, String> params, RouterBase router) {
        ActiveScreen previous = activeScreens.remove(stage);
        if (previous != null) {
            log.debug("Destroying previous screen on stage");
            previous.ctx().scope().cancel();
            previous.screen().onDestroy();
        }

        ScreenContextBase ctx = new ScreenContextBase(stage, router, this);
        ctx.setParams(params);

        ScreenComponent screen;
        try {
            screen = route.factory().create(ctx);
            log.debug("Screen created for route '{}'", route.identification());
        } catch (Exception e) {
            log.error("Failed to create screen for route '{}'", route.identification(), e);
            ErrorReporter.handle(e);
            throw new RouteResolutionException(route.identification(), e);
        }

        activeScreens.put(stage, new ActiveScreen(screen, ctx));

        ComponentInterface<?> view = extractView(screen);
        log.debug("Calling onMount() for route '{}'", route.identification());
        screen.onMount();
        return new RouteResult(view, route.props());
    }

    /** Registers a window spawned via {@link ScreenContextBase#spawnWindow(String)}. */
    public void registerSpawned(Stage stage) {
        spawnedWindows.add(stage);
    }

    /**
     * Removes a spawned window from tracking and destroys its active screen.
     * Does not close the stage (used when the user closes the window).
     */
    public void destroy(Stage stage) {
        spawnedWindows.removeIf(w -> w == stage);
        ActiveScreen active = activeScreens.remove(stage);
        if (active != null) {
            active.ctx().scope().cancel();
            active.screen().onDestroy();
        }
    }

    /** Destroys every spawned window's screen and closes its stage. */
    public void closeAllSpawned() {
        log.info("Closing {} spawned window(s)", spawnedWindows.size());
        List<Stage> toClose = new ArrayList<>(spawnedWindows);
        for (Stage stage : toClose) {
            destroy(stage);
            stage.close();
        }
    }

    private ComponentInterface<?> extractView(Object screen) {
        if (screen instanceof ComponentInterface<?> view) {
            log.debug("Screen implements ComponentInterface directly");
            return view;
        }

        try {
            var method = screen.getClass().getMethod("render");
            Object result = method.invoke(screen);

            if (!(result instanceof ComponentInterface<?> component)) {
                var e = new IllegalStateException(
                        "render() de " + screen.getClass().getSimpleName()
                                + " deve retornar ComponentInterface"
                );
                log.error("render() did not return ComponentInterface for {}", screen.getClass().getSimpleName(), e);
                ErrorReporter.handle(e);
                throw e;
            }

            return component;
        } catch (NoSuchMethodException e) {
            var wrapped = new IllegalStateException(
                    "Screen " + screen.getClass().getSimpleName()
                            + " deve expor render() retornando ComponentInterface",
                    e
            );
            log.error("Screen {} does not expose render() returning ComponentInterface", screen.getClass().getSimpleName(), e);
            ErrorReporter.handle(wrapped);
            throw wrapped;
        } catch (Exception e) {
            var wrapped = new IllegalStateException(
                    "Falha ao invocar render() em " + screen.getClass().getSimpleName(),
                    e
            );
            log.error("Failed to invoke render() on {}", screen.getClass().getSimpleName(), e);
            ErrorReporter.handle(wrapped);
            throw wrapped;
        }
    }
}