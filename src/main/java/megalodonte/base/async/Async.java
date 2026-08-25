package megalodonte.base.async;

import megalodonte.application.ErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for running tasks on virtual threads. Uses a
 * {@code newVirtualThreadPerTaskExecutor} — each submitted task gets its own
 * virtual thread.
 * <p>
 * This is fire-and-forget: unhandled exceptions go to {@link ErrorReporter},
 * there is no result callback and no handle to cancel. For work that opens
 * a resource needing symmetric teardown, use {@link Scope} instead.
 */
public class Async {
    private static final Logger log = LoggerFactory.getLogger(Async.class);
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private Async(){}

    /**
     * Submits a task to run on a virtual thread. Exceptions are caught and
     * forwarded to {@link ErrorReporter#handle}.
     *
     * @param task the task to execute
     */
    public static void Run(RunnableThrowing task) {
            executor.submit(() -> {
                try {
                    task.run();
                } catch (Throwable t) {
                    log.error("Async task failed", t);
                    ErrorReporter.handle(t);
                }
            });
        }

}
