package megalodonte.application.messaging;

import java.util.List;
import static megalodonte.application.messaging.NativeUi.*;

/** Separate JVM fixture: stdout is protocol only. */
public final class NativeMessagingWorkerFixture {
    public static void main(String[] args) throws Exception {
        String mode = args[0];
        if (mode.equals("malformed")) { System.out.println("not JSON"); return; }
        NativeWorker.run(new NativeWorker.Application() {
            String input = "initial";
            int clicks;
            public Snapshot prepare(State state) throws Exception {
                input = state.input("one", "name", "initial");
                if (mode.equals("timeout")) Thread.sleep(30_000);
                return snapshot();
            }
            public Snapshot onEvent(Event event) {
                if (event.type().equals("change")) { input = event.value(); return null; }
                if (event.component().equals("crash")) System.exit(7);
                clicks++;
                return snapshot();
            }
            private Snapshot snapshot() {
                var first = new Window("one", "First " + mode, 320, 180,
                        column("root", input("name", input), text("version", mode + ":" + clicks),
                                button("save", "Save"), button("crash", "Crash worker")));
                if (mode.equals("missing")) return new Snapshot(List.of(first));
                Element second = mode.equals("invalid") ? new Element("root", null, "", List.of())
                        : column("root", text("version", mode));
                return new Snapshot(List.of(first, new Window("two", "Second", 320, 180, second)));
            }
        });
    }
}
