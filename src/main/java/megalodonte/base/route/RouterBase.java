package megalodonte.base.route;

import megalodonte.application.Context;

/**
 * Contract for routers that resolve routes and produce screen views.
 * Implemented by the concrete router in {@code megalodonte-router}.
 */
public interface RouterBase {
    /** Binds this router to the application context, enabling navigation. */
    void bind(Context context);

    /** Returns the entrypoint route result for the default route. */
    RouteResult entrypoint();
}
