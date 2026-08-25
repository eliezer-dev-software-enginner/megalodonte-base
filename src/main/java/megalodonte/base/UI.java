package megalodonte.base;

import javafx.application.Platform;

/**
 * Helper for running code on the JavaFX Application Thread.
 * Wraps {@link Platform#runLater} for use from virtual threads or background work.
 */
public final class UI {

    /**
     * Executes {@code task} on the JavaFX Application Thread.
     * If already on the FX thread, runs immediately; otherwise schedules it.
     *
     * @param task the runnable to execute on the FX thread
     */
    public static void runOnUi(Runnable task){
        Platform.runLater(task);
    }
}
