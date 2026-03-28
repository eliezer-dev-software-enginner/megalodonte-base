package megalodonte.application;

import megalodonte.base.Component;

public interface ScreenComponent {
    Component render();
    
    default void onMount() {
        // Implementação vazia - método opcional
    }
}
