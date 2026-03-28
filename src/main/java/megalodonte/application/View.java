package megalodonte.application;

import megalodonte.base.components.ComponentInterface;

@Deprecated(forRemoval = true)
public interface View {
    ComponentInterface<?> render();
}