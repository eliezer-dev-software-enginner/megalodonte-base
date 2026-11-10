package megalodonte.application.messaging;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Experimental permanent JavaFX host. Owns Stage/Scene; workers own no native windows.
 * Call public methods on the FX thread. This is not yet an adapter for existing screens.
 */
public final class NativeWindowHost implements AutoCloseable {
    private record Outgoing(String type, Object body) {}
    private static final class Worker {
        final long generation;
        final Process process;
        final NativeWire wire;
        final BlockingQueue<Outgoing> outgoing = new ArrayBlockingQueue<>(64);
        final CompletableFuture<Long> ready = new CompletableFuture<>();
        Thread writer;
        int inFlight;
        Worker(long generation, Process process) {
            this.generation = generation; this.process = process;
            wire = new NativeWire(process.getInputStream(), process.getOutputStream());
        }
        void send(String type, Object body) throws IOException {
            if (!outgoing.offer(new Outgoing(type, body))) throw new IOException("Native worker queue is full");
        }
        void stop() {
            if (writer != null) writer.interrupt();
            process.destroy();
            Thread.ofVirtual().start(() -> {
                try { if (!process.waitFor(2, TimeUnit.SECONDS)) process.destroyForcibly(); }
                catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            });
        }
    }
    private final Map<String, Stage> windows = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> inputs = new LinkedHashMap<>();
    private final Consumer<Throwable> errors;
    private final long timeoutMillis;
    private Worker active;
    private Worker candidate;
    private long generation;
    private boolean closed;

    public NativeWindowHost(Consumer<Throwable> errors) { this(errors, Duration.ofSeconds(15)); }
    public NativeWindowHost(Consumer<Throwable> errors, Duration preparationTimeout) {
        this.errors = Objects.requireNonNull(errors);
        timeoutMillis = preparationTimeout.toMillis();
        if (timeoutMillis <= 0) throw new IllegalArgumentException("Positive timeout required");
    }
    /** Retains window identities. Rejects replacement until outstanding UI events finish. */
    public CompletableFuture<Long> replaceWorker(List<String> command) {
        requireFx();
        if (closed || candidate != null || (active != null && active.inFlight != 0))
            return CompletableFuture.failedFuture(new IllegalStateException("Host closed, replacing, or processing events"));
        try {
            Process process = new ProcessBuilder(List.copyOf(command)).redirectError(ProcessBuilder.Redirect.INHERIT).start();
            Worker next = new Worker(++generation, process);
            candidate = next;
            interactive(false);
            next.writer = Thread.ofVirtual().name("native-host-writer").start(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        var outgoing = next.outgoing.take();
                        next.wire.send(next.generation, outgoing.type, outgoing.body);
                    }
                } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                catch (Exception error) { Platform.runLater(() -> failed(next, error)); }
            });
            next.send("HELLO", new NativeUi.State(inputs));
            Thread.ofVirtual().name("native-host-reader").start(() -> read(next));
            CompletableFuture.delayedExecutor(timeoutMillis, TimeUnit.MILLISECONDS).execute(() ->
                    Platform.runLater(() -> {
                        if (candidate == next) failed(next, new TimeoutException("Native candidate did not become ready"));
                    }));
            return next.ready;
        } catch (IOException error) {
            if (candidate != null) failed(candidate, error);
            return CompletableFuture.failedFuture(error);
        }
    }
    /** Read-only registry for host integrations; returned Stages remain host-owned. */
    public Map<String, Stage> windows() { requireFx(); return Map.copyOf(windows); }
    public long activeGeneration() { requireFx(); return active == null ? 0 : active.generation; }
    public boolean readyForReplacement() {
        requireFx(); return !closed && candidate == null && (active == null || active.inFlight == 0);
    }

    private void read(Worker worker) {
        try {
            for (NativeWire.Message message; (message = worker.wire.read()) != null;) {
                if (message.generation() != worker.generation) throw new IOException("Stale native generation");
                var received = message;
                var handled = new CompletableFuture<Void>();
                Platform.runLater(() -> {
                    try { receive(worker, received); handled.complete(null); }
                    catch (Exception error) { failed(worker, error); handled.completeExceptionally(error); }
                });
                handled.get(); // Backpressure: do not flood the FX event queue.
            }
            throw new IOException("Native worker disconnected; last UI retained");
        } catch (Exception error) { Platform.runLater(() -> failed(worker, error)); }
    }
    private void receive(Worker worker, NativeWire.Message message) throws IOException {
        if (closed || (worker != candidate && worker != active)) return;
        if (message.type().equals("ACK") && worker == active && worker.inFlight > 0) {
            worker.inFlight--; return;
        }
        if (!message.type().equals("SNAPSHOT")) throw new IOException("Expected native SNAPSHOT");
        var snapshot = message.body(NativeUi.Snapshot.class);
        var prepared = prepare(snapshot, worker);
        if (worker == candidate) {
            if (!worker.process.isAlive()) throw new IOException("Candidate exited during preparation");
            Worker previous = active;
            commit(snapshot, prepared);
            active = worker; candidate = null;
            worker.send("ACTIVATE", Map.of());
            interactive(true);
            worker.ready.complete(worker.generation);
            if (previous != null) previous.stop();
        } else {
            commit(snapshot, prepared);
            if (worker.inFlight > 0) worker.inFlight--;
            interactive(candidate == null);
        }
    }
    private Map<String, Parent> prepare(NativeUi.Snapshot snapshot, Worker worker) {
        if (snapshot == null || snapshot.windows().isEmpty() || snapshot.windows().size() > 32)
            throw new IllegalArgumentException("Expected 1 to 32 native windows");
        Map<String, Parent> prepared = new LinkedHashMap<>();
        int[] nodes = {0};
        for (var window : snapshot.windows()) {
            id(window.id());
            if (window.title() == null || window.title().length() > 512 || !Double.isFinite(window.width())
                    || !Double.isFinite(window.height()) || window.width() < 1 || window.width() > 8192
                    || window.height() < 1 || window.height() > 8192 || prepared.containsKey(window.id()))
                throw new IllegalArgumentException("Invalid native window");
            Node node = render(window.id(), window.root(), new HashSet<>(), worker, nodes, 0);
            prepared.put(window.id(), node instanceof Parent parent ? parent : new StackPane(node));
        }
        if (!prepared.keySet().containsAll(windows.keySet()))
            throw new IllegalArgumentException("Removing existing windows requires an explicit migration");
        return prepared;
    }
    private Node render(String window, NativeUi.Element element, Set<String> identifiers,
                        Worker worker, int[] count, int depth) {
        if (element == null || element.kind() == null || ++count[0] > 5000 || depth > 64)
            throw new IllegalArgumentException("Invalid or excessive native component tree");
        id(element.id());
        if (!identifiers.add(element.id()) || element.text() == null || element.text().length() > 65536)
            throw new IllegalArgumentException("Duplicate component or invalid text");
        boolean container = element.kind() == NativeUi.Kind.COLUMN || element.kind() == NativeUi.Kind.ROW;
        if (!container && !element.children().isEmpty()) throw new IllegalArgumentException("Leaf has children");
        Node result = switch (element.kind()) {
            case COLUMN, ROW -> {
                Pane pane = element.kind() == NativeUi.Kind.COLUMN ? new VBox(8) : new HBox(8);
                for (var child : element.children()) pane.getChildren().add(render(window, child, identifiers, worker, count, depth + 1));
                yield pane;
            }
            case TEXT -> new Label(element.text());
            case BUTTON -> {
                var button = new Button(element.text());
                button.setOnAction(event -> event(worker, new NativeUi.Event(window, element.id(), "click", "")));
                yield button;
            }
            case INPUT -> {
                var field = new TextField(inputs.getOrDefault(window, Map.of()).getOrDefault(element.id(), element.text()));
                field.textProperty().addListener((observable, old, value) -> {
                    if (worker != active || candidate != null) return;
                    inputs.computeIfAbsent(window, key -> new LinkedHashMap<>()).put(element.id(), value);
                    event(worker, new NativeUi.Event(window, element.id(), "change", value));
                });
                yield field;
            }
        };
        result.setId(element.id());
        return result;
    }
    private void commit(NativeUi.Snapshot snapshot, Map<String, Parent> roots) {
        var oldRoots = new LinkedHashMap<String, Parent>();
        var oldTitles = new LinkedHashMap<String, String>();
        var added = new ArrayList<String>();
        try {
            for (var window : snapshot.windows()) {
                Stage stage = windows.get(window.id());
                if (stage == null) {
                    stage = new Stage(); stage.setScene(new Scene(roots.get(window.id()), window.width(), window.height()));
                    windows.put(window.id(), stage); added.add(window.id());
                } else {
                    oldRoots.put(window.id(), stage.getScene().getRoot()); oldTitles.put(window.id(), stage.getTitle());
                    stage.getScene().setRoot(roots.get(window.id()));
                }
                stage.setTitle(window.title());
            }
            for (String id : added) windows.get(id).show();
        } catch (RuntimeException failure) {
            for (String id : added) windows.remove(id).close();
            oldRoots.forEach((id, root) -> { windows.get(id).getScene().setRoot(root); windows.get(id).setTitle(oldTitles.get(id)); });
            throw failure;
        }
    }
    private void event(Worker worker, NativeUi.Event event) {
        if (closed || worker != active || candidate != null) return;
        try { worker.send("EVENT", event); worker.inFlight++; }
        catch (IOException error) { failed(worker, error); }
    }
    private void failed(Worker worker, Throwable error) {
        if (worker != candidate && worker != active) return;
        if (worker == candidate) {
            candidate = null; worker.ready.completeExceptionally(error);
            interactive(active != null);
        } else { active = null; interactive(false); }
        worker.stop(); errors.accept(error);
    }
    private void interactive(boolean enabled) {
        windows.values().forEach(stage -> stage.getScene().getRoot().setDisable(!enabled));
    }
    private static void id(String id) {
        if (id == null || !id.matches("[A-Za-z0-9_.-]{1,128}")) throw new IllegalArgumentException("Invalid native identity");
    }
    private static void requireFx() {
        if (!Platform.isFxApplicationThread()) throw new IllegalStateException("Native host requires the FX thread");
    }
    @Override public void close() {
        requireFx(); closed = true;
        if (candidate != null) { candidate.ready.completeExceptionally(new CancellationException("Host closed")); candidate.stop(); }
        if (active != null) active.stop();
        candidate = active = null;
        windows.values().forEach(Stage::close); windows.clear(); inputs.clear();
    }
}
