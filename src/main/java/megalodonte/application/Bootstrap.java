package megalodonte.application;

import javafx.stage.Stage;

import java.util.function.Consumer;

public final class Bootstrap {
    // Context e Event têm ciclos de vida diferentes — o contexto chega uma vez no startup, e eventos chegam depois, assincronamente.
    // Separá-los em dois consumers deixa o design mais claro e resolve o problema estruturalmente.
    public static Consumer<Context> handler;
    public static Consumer<MegalodonteApp.Event> eventHandler;
    public static Runnable closehandler;

    public static void dispatch(Stage stage) {
        var context = new Context(stage);
        MegalodonteApp.setCurrentContext(context);

        if (handler != null) {
            handler.accept(context);
        }

        stage.show();
    }

    public static void dispatchEvent(MegalodonteApp.Event event) {
        if (eventHandler != null) {
            eventHandler.accept(event);
        }
    }
}