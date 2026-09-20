package megalodonte.base.route;

import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;

@FunctionalInterface
public interface ScreenFactory {
    ScreenComponent create(ScreenContextInterface ctx) throws Exception;
}