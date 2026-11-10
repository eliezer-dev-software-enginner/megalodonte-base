package megalodonte.application.messaging;

import javafx.application.Platform;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/** CLI control channel for the experimental native host; stdout is JSON protocol. */
public final class NativeHostSupervisor {
    private NativeHostSupervisor() {}
    record Launch(List<String> command, String directory, Map<String, String> environment) {}
    private static <T> T fx(Callable<T> callback) throws Exception {
        var task = new FutureTask<>(callback);
        Platform.runLater(task);
        return task.get();
    }
    static void main() throws Exception {
        var started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        started.await();
        var host = fx(() -> new NativeWindowHost(error -> System.err.println("[native-host] " + error.getMessage())));
        var shutdown = new Thread(host::terminateWorkers, "native-host-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
        var wire = new NativeWire(System.in, System.out);
        try {
            for (NativeWire.Message message; (message = wire.read()) != null;) {
                if (message.generation() != 1) throw new IOException("Invalid native supervisor session");
                if (message.type().equals("CLOSE")) break;
                if (!message.type().equals("REPLACE")) throw new IOException("Expected REPLACE or CLOSE");
                try {
                    Launch launch = message.body(Launch.class);
                    long generation = fx(() -> host.replaceWorker(launch.command(), Path.of(launch.directory()), launch.environment())).get();
                    Map<String, String> windows = fx(() -> {
                        host.windows().values().forEach(stage -> stage.setOnCloseRequest(event -> {
                            event.consume(); host.close(); Platform.exit(); System.exit(0);
                        }));
                        var identities = new LinkedHashMap<String, String>();
                        host.windows().forEach((id, stage) -> identities.put(id,
                                Integer.toHexString(System.identityHashCode(stage)) + ":" + Integer.toHexString(System.identityHashCode(stage.getScene()))));
                        return identities;
                    });
                    wire.send(1, "RESULT", Map.of("request", message.sequence(), "status", "APPLIED",
                            "generation", generation, "windows", windows.size(), "identities", windows,
                            "workerPid", fx(host::activeWorkerPid)));
                } catch (Exception error) {
                    Throwable cause = error instanceof ExecutionException ? error.getCause() : error;
                    wire.send(1, "RESULT", Map.of("request", message.sequence(), "status", "REJECTED",
                            "detail", String.valueOf(cause.getMessage())));
                }
            }
        } finally {
            host.terminateWorkers();
            fx(() -> { host.close(); return null; });
            Platform.exit();
            Runtime.getRuntime().removeShutdownHook(shutdown);
        }
    }
}
