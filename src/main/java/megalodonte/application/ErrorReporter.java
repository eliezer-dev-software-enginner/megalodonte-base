package megalodonte.application;

/**
 * Central error handler for the framework. Unhandled exceptions from
 * {@link megalodonte.base.async.Async} and {@link megalodonte.base.async.Scope}
 * are routed here.
 * <p>
 * Applications should register a UI-specific handler via {@link #register} at
 * bootstrap time. If none is registered, a fallback prints to stderr.
 */
public final class ErrorReporter {

    /** Functional interface for error handling callbacks. */
    @FunctionalInterface
    public interface Handler {
        void onError(Throwable t);
    }

    private static Handler handler = ErrorReporter::defaultHandler;

    private ErrorReporter() {}

    /** Registers the application-specific error handler. Called once at bootstrap.
     * @param appHandler the error handler implementation
     */
    public static void register(Handler appHandler) {
        handler = appHandler;
    }

    /**
     * Reports an error by logging it and delegating to the registered handler.
     *
     * @param t the throwable to report
     */
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