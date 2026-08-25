package megalodonte.base.async;

import megalodonte.application.ErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Cancellation binding between async work ({@link Async#Run}) and the lifecycle of
 * whoever initiated it (a screen, a ViewModel). Solves the same class of race that
 * {@code viewModelScope}/{@code DisposableEffect} solve in Jetpack Compose: cancelling is
 * synchronous and cheap (a flag), but the work itself only knows it was cancelled when it
 * checks — hence {@link #run} and {@link #onCancel} exist, instead of trying to force-interrupt.
 * <p>
 * One instance covers a single lifecycle (a screen, a ViewModel) and is not reusable
 * after being cancelled.
 */
public final class Scope {
    private static final Logger log = LoggerFactory.getLogger(Scope.class);
    private volatile boolean cancelled = false;
    private final List<Runnable> onCancelCallbacks = new ArrayList<>();

    /**
     * Runs {@code task} on a virtual thread, but only if the scope hasn't been cancelled
     * at the moment the task begins executing. If {@link #cancel()} has already run before
     * that, {@code task} is never called.
     * <p>
     * This closes the race "onDestroy runs before Async.Run starts" — but not on its own
     * the race "onDestroy runs in the middle of task", which still depends on {@code task}
     * checking {@link #isCancelled()} at the right points (e.g. right before opening a
     * resource) or registering cleanup via {@link #onCancel}.
     */
    public void run(RunnableThrowing task) {
        Async.Run(() -> {
            if (cancelled) {
                log.debug("Scope already cancelled, skipping task");
                return;
            }
            task.run();
        });
    }

    /**
     * Registers {@code cleanup} to run when the scope is cancelled. If already cancelled
     * at the time of the call, runs {@code cleanup} immediately on the calling thread —
     * covers the case where the resource finished being acquired after {@link #cancel()}
     * already ran (e.g. connection that finished opening after the screen was already
     * destroyed).
     */
    public void onCancel(Runnable cleanup) {
        boolean alreadyCancelled;
        synchronized (onCancelCallbacks) {
            alreadyCancelled = cancelled;
            if (!alreadyCancelled) onCancelCallbacks.add(cleanup);
        }
        if (alreadyCancelled) runCleanup(cleanup);
    }

    /**
     * Cancels the scope: {@link #isCancelled()} starts returning {@code true} and all
     * callbacks registered via {@link #onCancel} run now, on the same thread (synchronous,
     * just like {@code onDestroy()} — cancelling is cheap, the actual cleanup work is
     * each callback's responsibility).
     */
    public void cancel() {
        List<Runnable> callbacks;
        synchronized (onCancelCallbacks) {
            if (cancelled) return;
            cancelled = true;
            callbacks = new ArrayList<>(onCancelCallbacks);
            onCancelCallbacks.clear();
        }
        log.debug("Scope cancelled, running {} cleanup callback(s)", callbacks.size());
        for (Runnable cleanup : callbacks) runCleanup(cleanup);
    }

    private void runCleanup(Runnable cleanup) {
        try {
            cleanup.run();
        } catch (Throwable t) {
            ErrorReporter.handle(t);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
