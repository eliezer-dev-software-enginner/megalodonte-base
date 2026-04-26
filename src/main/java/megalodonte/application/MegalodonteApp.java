package megalodonte.application;

import javafx.application.Application;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MegalodonteApp {

    public enum Event{
        CloseRequest
    }

    private static Context currentContext;

    public static Context getCurrentContext() {
        return currentContext;
    }

    static void setCurrentContext(Context context) {
        currentContext = context;
    }

    public static void run(Consumer<Context> contextHandler, Consumer<Event> onEvent) {
        Bootstrap.handler = contextHandler;
        Bootstrap.eventHandler = onEvent;
        Application.launch(JavaFXHost.class);
    }

    public static void run(Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(JavaFXHost.class);
    }

    public static void run(String[] args, Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(JavaFXHost.class, args);
    }
}
