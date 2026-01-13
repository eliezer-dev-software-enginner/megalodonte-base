package megalodonte.base;

import javafx.application.Platform;

public final class UI {
    public static void runOnUi(Runnable task){
        Platform.runLater(task);
    }
}
