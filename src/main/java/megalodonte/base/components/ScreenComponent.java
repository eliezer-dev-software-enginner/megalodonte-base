package megalodonte.base.components;

public interface ScreenComponent {
    Component render();
    
    default void onMount() {
        // Implementação vazia - método opcional
    }

    default void onDestroy() {
        // Implementação vazia - método opcional
    }
}
