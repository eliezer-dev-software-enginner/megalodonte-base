package megalodonte.application.messaging;

import javafx.application.Platform;
import java.util.List;

/** Experimental standalone host. Arguments are the worker executable and arguments. */
public final class NativeHostLauncher {
    private NativeHostLauncher() {}
    static void main(String[] args) {
        if (args.length == 0) throw new IllegalArgumentException("Pass a worker executable and its arguments");
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            var host = new NativeWindowHost(error -> System.err.println("[native-host] " + error.getMessage()));
            host.replaceWorker(List.of(args)).whenComplete((generation, error) -> {
                if (error != null) { host.close(); Platform.exit(); return; }
                // Prototype session lifecycle: closing any window closes this host session.
                host.windows().values().forEach(stage -> stage.setOnCloseRequest(event -> {
                    event.consume(); host.close(); Platform.exit();
                }));
            });
        });
    }
}
