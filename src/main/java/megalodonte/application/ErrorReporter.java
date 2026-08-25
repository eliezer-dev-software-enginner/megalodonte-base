package megalodonte.application;

public final class ErrorReporter {

    @FunctionalInterface
    public interface Handler {
        void onError(Throwable t);
    }

    private static Handler handler = ErrorReporter::defaultHandler;

    private ErrorReporter() {}

    /** Called by the app at bootstrap to plug in the specific error UI. */
    public static void register(Handler appHandler) {
        handler = appHandler;
    }

    public static void handle(Throwable t) {
        log(t);
        handler.onError(t);
    }

    private static void defaultHandler(Throwable t) {
        // Fallback when the app hasn't registered anything — must never fail silently
        System.err.println("[ErrorReporter] No handler registered. Error: " + t.getMessage());
        t.printStackTrace();
    }

    private static void log(Throwable t) {
        // TODO: file logging, same pattern as the close log already in Main
    }
}