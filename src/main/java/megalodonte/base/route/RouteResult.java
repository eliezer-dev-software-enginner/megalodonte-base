package megalodonte.base.route;

import megalodonte.base.components.ComponentInterface;

public record RouteResult(
        ComponentInterface<?> view,
        RouteProps props
) {}
