package megalodonte.application;

import javafx.application.Application;

import java.util.function.Consumer;

public final class MegalodonteApp {

    private static Runnable onShutdown;

    public static void run(Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(JavaFXHost.class);
    }

    public static void run(String[] args, Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(JavaFXHost.class, args);
    }

    public static void onShutdown(Runnable callback) {
        onShutdown = callback;
    }

    static void triggerShutdown() {
        if (onShutdown != null) {
            try {
                onShutdown.run();
            } catch (Exception e) {
                System.err.println("Erro no onShutdown: " + e.getMessage());
            }
        }
    }
}
