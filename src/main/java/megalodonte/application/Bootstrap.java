package megalodonte.application;

import javafx.stage.Stage;

import java.util.function.Consumer;

public final class Bootstrap {

    public static Consumer<Context> handler;

    public static void dispatch(Stage stage) {
        var context = new Context(stage);
        MegalodonteApp.setCurrentContext(context);

        if (handler != null) {
            handler.accept(context);
        }

        if (context.shouldAutoShow() && !stage.isShowing()) {
            stage.show();
        }
    }
}