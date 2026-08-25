package megalodonte.base.route;

import megalodonte.base.components.ComponentInterface;

/**
 * Result of resolving a route. Pairs a {@link ComponentInterface} view with
 * its {@link RouteProps} metadata.
 *
 * @param view  the screen component to render
 * @param props the route's display properties
 */
public record RouteResult(
        ComponentInterface<?> view,
        RouteProps props
) {}
