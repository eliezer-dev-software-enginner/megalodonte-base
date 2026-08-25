package megalodonte.base.components;

/**
 * Interface for screen-level components managed by the router. A screen has
 * a lifecycle: {@link #render()} produces the component tree, {@link #onMount()}
 * is called once the stage is visible, and {@link #onDestroy()} is called
 * when the screen is navigated away from.
 */
public interface ScreenComponent {
    /** Renders the screen's component tree. Called once per navigation. */
    Component render();
    
    /** Called once after the stage is visible and the scene is attached. Override for one-time setup. */
    default void onMount() {
        // Empty implementation — optional method
    }

    /** Called when the screen is destroyed (navigated away from). Override for cleanup. */
    default void onDestroy() {
        // Empty implementation — optional method
    }
}
