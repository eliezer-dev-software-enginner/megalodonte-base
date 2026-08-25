package megalodonte.base.async;

import megalodonte.application.ErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {
    private static final Logger log = LoggerFactory.getLogger(Async.class);
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private Async(){}

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
