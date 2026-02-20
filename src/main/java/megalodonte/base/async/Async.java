package megalodonte.base.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private Async(){}

    public static void Run(Runnable task){
        executor.execute(task);
    }
}
