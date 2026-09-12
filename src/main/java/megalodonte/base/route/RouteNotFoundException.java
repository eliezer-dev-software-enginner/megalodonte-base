package megalodonte.base.route;

/**
 * Exception thrown when a route cannot be resolved by the Router.
 *
 * <p>This exception indicates that no registered route matches
 * the provided route identification, including dynamic segments.</p>
 *
 * <p>It is considered a usage or configuration error and is therefore
 * implemented as an unchecked exception.</p>
 */
public class RouteNotFoundException extends RuntimeException {

    /**
     * Creates a new exception for a missing route.
     *
     * @param identification the route identification that could not be resolved
     */
    public RouteNotFoundException(String identification) {
        super("Route not found: " + identification);
    }
}
