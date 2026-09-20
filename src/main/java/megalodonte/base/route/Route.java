package megalodonte.base.route;

public record Route(
            String identification,
            ScreenFactory factory,
            RouteProps props
    ) {}