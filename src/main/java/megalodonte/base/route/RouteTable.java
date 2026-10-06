package megalodonte.base.route;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores the route definitions and the entrypoint. Owns route matching, so both
 * the router (navigation) and {@link megalodonte.base.route.v2.ScreenContextBase}
 * (spawn) resolve routes through the same registry.
 */
public final class RouteTable {
    public record ResolvedRoute(Route route, Map<String, String> params) {}

    private final Set<Route> routes;
    private final String entrypoint;
    private final megalodonte.contracts.RouteMatcher<Route> matcher;

    public RouteTable(Set<Route> routes, String entrypoint) {
        this.routes = Set.copyOf(routes);
        this.entrypoint = entrypoint;
        matcher = new megalodonte.contracts.RouteMatcher<>(this.routes, Route::identification);
    }

    public String entrypoint() {
        return entrypoint;
    }

    public Set<Route> routes() {
        return routes;
    }

    /**
     * Resolves the given path against the registered routes, extracting
     * dynamic segment parameters ({@code ${name}}).
     *
     * @param path the route identification to resolve (e.g. "user/123")
     * @return the matched route and its parameters
     * @throws RouteNotFoundException if no route matches the given path
     */
    public ResolvedRoute resolve(String path) {
        return matcher.resolve(path).map(match -> new ResolvedRoute(match.route(), match.params()))
            .orElseThrow(() -> new RouteNotFoundException(path));
    }
}
