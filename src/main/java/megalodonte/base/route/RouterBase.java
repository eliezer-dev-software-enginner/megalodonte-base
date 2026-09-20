package megalodonte.base.route;

import javafx.stage.Stage;
import megalodonte.application.Context;

/**
 * Contract for routers that resolve routes and produce screen views.
 * Implemented by the concrete router in {@code megalodonte-router}.
 *
 * <p>Navigation-only: route storage lives in {@link RouteTable} and window
 * spawning lives in {@code megalodonte.base.route.v2.ScreenContextBase}.</p>
 */
public interface RouterBase {
    /** Binds this router to the application context, enabling navigation. */
    void bind(Context context);

    /** Returns the entrypoint route result for the default route. */
    RouteResult entrypoint();

    RouteResult navigateOnStage(String path, Stage selfStage);

    RouteResult navigateAndCloseOthers(String path);

    Stage mainStage();
}