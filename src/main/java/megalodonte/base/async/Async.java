package megalodonte.base.async;

import megalodonte.application.ErrorReporter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private Async(){}

    public static void Run(RunnableThrowing task) {
            executor.submit(() -> {
                try {
                    task.run();
                } catch (Throwable t) {
                    ErrorReporter.handle(t);
                }
            });
        }

}
