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

    public RouteTable(Set<Route> routes, String entrypoint) {
        this.routes = Set.copyOf(routes);
        this.entrypoint = entrypoint;
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
        String[] pathParts = path.split("/");

        for (Route route : routes) {
            String[] routeParts = route.identification().split("/");
            if (routeParts.length != pathParts.length) continue;

            Map<String, String> params = new HashMap<>();
            boolean matched = true;

            for (int i = 0; i < routeParts.length; i++) {
                String rp = routeParts[i];
                String pp = pathParts[i];

                if (rp.startsWith("${") && rp.endsWith("}")) {
                    params.put(rp.substring(2, rp.length() - 1), pp);
                } else if (!rp.equals(pp)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                return new ResolvedRoute(route, params);
            }
        }

        throw new RouteNotFoundException(path);
    }
}