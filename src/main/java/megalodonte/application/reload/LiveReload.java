package megalodonte.application.reload;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.components.Component;
import megalodonte.base.state.SubscriptionScope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Development-only screen refresh. No class-loader replacement or lifecycle restart. */
public final class LiveReload {
    private static final Object KEY = new Object();
    private static final Object CAPTURE = new Object();
    private static final Object WINDOW_ID = new Object();
    /** FX-thread-owned registry. Weak keys avoid retaining a Stage after its lifecycle ends. */
    private static final WeakHashMap<Stage, Boolean> TRACKED_STAGES = new WeakHashMap<>();
    private static int secondaryWindowSequence;
    private static RestartWindowState.Snapshot restartSnapshot;
    private static boolean restartSnapshotLoaded;
    private record Binding(ScreenComponent screen, Parent root, SubscriptionScope scope) {}
    private record Replacement(Stage stage, Scene scene, Binding old, Binding next) {}

    private LiveReload() {}

    /** Give the application stage a stable identity before the startup handler creates its view. */
    public static void markPrimary(Stage stage) {
        if (ReloadAgent.isActive()) stage.getProperties().put(WINDOW_ID, "primary");
    }

    /** Render with subscription ownership only in a development-agent session. */
    public static Component render(ScreenComponent screen) {
        if (!ReloadAgent.isActive()) return screen.render();
        var captured = SubscriptionScope.capture(screen::render);
        try {
            if (!(captured.value().getJavaFxNode() instanceof Parent))
                throw new IllegalStateException("Screen render must return a Parent root");
            captured.value().getJavaFxNode().getProperties().put(CAPTURE, captured.scope());
            return captured.value();
        } catch (RuntimeException | Error failure) {
            captured.scope().close();
            throw failure;
        }
    }

    /** Track only framework-managed, currently rendered screens, on the FX thread. */
    public static void track(Stage stage, ScreenComponent screen, Parent root) {
        if (ReloadAgent.isActive()) {
            stage.getProperties().computeIfAbsent(WINDOW_ID, ignored -> "window-" + (++secondaryWindowSequence));
            Binding binding = binding(screen, root);
            var previous = stage.getProperties().put(KEY, binding);
            TRACKED_STAGES.put(stage, Boolean.TRUE);
            if (previous instanceof Binding old) release(old);
        }
    }

    /** Restore saved bounds and native mode after application code has configured the stage. */
    public static boolean restoreBeforeShow(Stage stage) {
        if (!ReloadAgent.isActive()) return true;
        String id = (String) stage.getProperties().get(WINDOW_ID);
        RestartWindowState.Snapshot snapshot = restartSnapshot();
        RestartWindowState.Window window = id == null || snapshot == null ? null : snapshot.windows().get(id);
        if (window != null) RestartWindowState.restore(stage, window);
        return window == null || window.showing();
    }

    /** Preserve foreground/background intent after JavaFX maps the recreated native window. */
    public static void restoreFocusAfterShow(Stage stage) {
        if (!ReloadAgent.isActive()) return;
        String id = (String) stage.getProperties().get(WINDOW_ID);
        RestartWindowState.Snapshot snapshot = restartSnapshot();
        if (id == null || snapshot == null || !stage.isShowing()) return;
        Platform.runLater(() -> {
            if (!snapshot.applicationFocused()) stage.toBack();
            else if (id.equals(snapshot.focusedWindow())) stage.requestFocus();
        });
    }

    private static RestartWindowState.Snapshot restartSnapshot() {
        if (!restartSnapshotLoaded) {
            restartSnapshot = RestartWindowState.read();
            restartSnapshotLoaded = true;
        }
        return restartSnapshot;
    }

    /** Capture all framework-managed windows on the FX thread before the CLI terminates this child. */
    static int snapshotForRestart() throws Exception {
        var result = new CompletableFuture<Integer>();
        Platform.runLater(() -> {
            try {
                Map<String, RestartWindowState.Window> windows = new LinkedHashMap<>();
                String focused = null;
                for (Stage stage : List.copyOf(TRACKED_STAGES.keySet())) {
                    String id = (String) stage.getProperties().get(WINDOW_ID);
                    if (id == null) continue;
                    if (stage.isFocused()) focused = id;
                    windows.put(id, new RestartWindowState.Window(id, stage.getX(), stage.getY(),
                            stage.getWidth(), stage.getHeight(), stage.isMaximized(), stage.isFullScreen(),
                            stage.isIconified(), stage.isShowing()));
                }
                RestartWindowState.write(new RestartWindowState.Snapshot(focused != null, focused, windows));
                result.complete(windows.size());
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        try {
            return result.get();
        } catch (ExecutionException error) {
            if (error.getCause() instanceof Exception exception) throw exception;
            if (error.getCause() instanceof Error fatal) throw fatal;
            throw new IllegalStateException(error.getCause());
        }
    }

    /** Drop a route that is being destroyed or replaced by an unmanaged view. */
    public static void untrack(Stage stage) {
        TRACKED_STAGES.remove(stage);
        if (stage.getProperties().remove(KEY) instanceof Binding old) release(old);
    }

    private static Binding binding(ScreenComponent screen, Parent root) {
        return new Binding(screen, root, (SubscriptionScope) root.getProperties().remove(CAPTURE));
    }

    private static void release(Binding binding) {
        if (binding.scope() == null) return;
        try { binding.scope().close(); }
        catch (RuntimeException error) { System.err.println("[live-reload] Subscription cleanup failed: " + error); }
    }

    /**
     * Run bytecode replacement and prepare all new roots in one FX turn. Commit roots
     * only after every render succeeds. Existing screen fields/State and Stages survive.
     * render() must be side-effect free; arbitrary user side effects cannot be undone.
     */
    static int apply(ReloadAgent.Change change, ReloadAgent.Change rollback) throws Exception {
        var result = new CompletableFuture<Integer>();
        Platform.runLater(() -> {
            try {
                change.run();
                result.complete(refresh());
            } catch (Throwable error) {
                try {
                    rollback.run();
                } catch (Throwable rollbackFailure) {
                    var fatal = new ReloadAgent.RollbackFailure("Rollback failed; restart the development application", error);
                    fatal.addSuppressed(rollbackFailure);
                    result.completeExceptionally(fatal);
                    return;
                }
                result.completeExceptionally(error);
            }
        });
        // Never abandon a queued transaction on timeout: that would permit a late
        // commit after the supervisor has already declared the reload failed.
        try {
            return result.get();
        } catch (ExecutionException error) {
            if (error.getCause() instanceof Exception exception) throw exception;
            if (error.getCause() instanceof Error fatal) throw fatal;
            throw new IllegalStateException(error.getCause());
        }
    }

    static int refresh() {
        if (!Platform.isFxApplicationThread()) throw new IllegalStateException("FX thread required");
        List<Replacement> replacements = new ArrayList<>();
        try {
            for (Stage stage : List.copyOf(TRACKED_STAGES.keySet())) {
                if (!(stage.getProperties().get(KEY) instanceof Binding binding)) continue;
                Scene scene = stage.getScene();
                // Do not replace a stale screen during an asynchronous navigation transition.
                if (scene == null || scene.getRoot() != binding.root())
                    throw new IllegalStateException("Window is navigating; save again after the transition");
                Parent root = (Parent) render(binding.screen()).getJavaFxNode();
                Binding next = binding(binding.screen(), root);
                replacements.add(new Replacement(stage, scene, binding, next));
                if (root == binding.root() || root.getScene() != null || root.getParent() != null)
                    throw new IllegalStateException("render() must return an unattached root");
                for (Replacement previous : replacements) {
                    if (previous.stage() != stage && previous.next().root() == root)
                        throw new IllegalStateException("Windows cannot share a root");
                }
            }
        } catch (RuntimeException | Error failure) {
            replacements.forEach(item -> release(item.next()));
            throw failure;
        }
        try {
            for (Replacement item : replacements) {
                item.scene().setRoot(item.next().root());
                item.stage().getProperties().put(KEY, item.next());
            }
        } catch (RuntimeException | Error failure) {
            for (Replacement item : replacements) {
                item.scene().setRoot(item.old().root());
                item.stage().getProperties().put(KEY, item.old());
                release(item.next());
            }
            throw failure;
        }
        replacements.forEach(item -> release(item.old()));
        return replacements.size();
    }
}
