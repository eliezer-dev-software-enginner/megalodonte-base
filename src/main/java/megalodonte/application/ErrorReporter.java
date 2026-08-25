package megalodonte.application;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Central error handler for the framework. Unhandled exceptions from
 * {@link megalodonte.base.async.Async} and {@link megalodonte.base.async.Scope}
 * are routed here.
 * <p>
 * Applications should register a UI-specific handler via {@link #register} at
 * bootstrap time. If none is registered, a fallback prints to stderr.
 * <p>
 * All errors are also appended to {@code ~/.megalodonte/errors.log} for
 * post-mortem analysis.
 */
public final class ErrorReporter {

    /** Functional interface for error handling callbacks. */
    @FunctionalInterface
    public interface Handler {
        void onError(Throwable t);
    }

    private static final Path LOG_DIR = Path.of(System.getProperty("user.home"), ".megalodonte");
    private static final Path LOG_FILE = LOG_DIR.resolve("errors.log");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static Handler handler = ErrorReporter::defaultHandler;

    private ErrorReporter() {}

    /** Registers the application-specific error handler. Called once at bootstrap.
     * @param appHandler the error handler implementation
     */
    public static void register(Handler appHandler) {
        handler = appHandler;
    }

    /**
     * Reports an error by logging it to file and delegating to the registered handler.
     *
     * @param t the throwable to report
     */
    public static void handle(Throwable t) {
        logToFile(t);
        handler.onError(t);
    }

    private static void defaultHandler(Throwable t) {
        // Fallback when the app hasn't registered anything — must never fail silently
        System.err.println("[ErrorReporter] No handler registered. Error: " + t.getMessage());
        t.printStackTrace();
    }

    /**
     * Appends the error with timestamp and stack trace to {@code ~/.megalodonte/errors.log}.
     * Creates the directory and file on first use. Failures are silently ignored —
     * error logging must never prevent the application from handling the error.
     */
    private static void logToFile(Throwable t) {
        try {
            Files.createDirectories(LOG_DIR);

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            String entry = LocalDateTime.now().format(TIMESTAMP)
                    + " [" + Thread.currentThread().getName() + "] "
                    + t.getClass().getName() + ": " + t.getMessage()
                    + System.lineSeparator()
                    + stackTrace
                    + System.lineSeparator()
                    + "---" + System.lineSeparator();

            Files.writeString(LOG_FILE, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Best effort — never prevents the application from handling the error
        }
    }
}