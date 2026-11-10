package megalodonte.application.messaging;

import java.io.*;

/** Worker-side entry point. Logging must use stderr, never protocol stdout. */
public final class NativeWorker {
    private NativeWorker() {}
    public interface Application {
        /** Prepare without business side effects: the candidate is not active yet. */
        NativeUi.Snapshot prepare(NativeUi.State state) throws Exception;
        NativeUi.Snapshot onEvent(NativeUi.Event event) throws Exception;
        default void activated() throws Exception {}
    }
    public static void run(Application application) throws Exception {
        run(application, System.in, System.out);
    }
    public static void run(Application application, InputStream input, OutputStream output) throws Exception {
        var wire = new NativeWire(input, output);
        var hello = wire.read();
        if (hello == null || !hello.type().equals("HELLO")) throw new IOException("Expected native HELLO");
        long generation = hello.generation();
        wire.send(generation, "SNAPSHOT", application.prepare(hello.body(NativeUi.State.class)));
        boolean active = false;
        for (NativeWire.Message message; (message = wire.read()) != null;) {
            if (message.generation() != generation) throw new IOException("Stale native generation");
            if (message.type().equals("ACTIVATE") && !active) {
                active = true;
                application.activated();
            } else if (message.type().equals("EVENT") && active) {
                var next = application.onEvent(message.body(NativeUi.Event.class));
                if (next != null) wire.send(generation, "SNAPSHOT", next);
                else wire.send(generation, "ACK", java.util.Map.of());
            } else throw new IOException("Unexpected native message: " + message.type());
        }
    }
}
