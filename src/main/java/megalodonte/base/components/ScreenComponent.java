package megalodonte.base.components;

public interface ScreenComponent {
    Component render();
    
    default void onMount() {
        // Empty implementation — optional method
    }

    default void onDestroy() {
        // Empty implementation — optional method
    }
}
