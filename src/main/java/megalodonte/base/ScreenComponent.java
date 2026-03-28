package megalodonte.base;

import megalodonte.base.route.ScreenContextInterface;

public interface ScreenComponent {
    Component render();
    
    default void onMount() {
        // Implementação vazia - método opcional
    }

}
