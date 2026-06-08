package megalodonte.base.route;

import megalodonte.application.Context;

public interface RouterBase {
    void bind(Context context);
    RouteResult entrypoint();
}
