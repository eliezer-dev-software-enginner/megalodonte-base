package megalodonte.base.async;

/**
 * A {@link Runnable} variant that allows checked exceptions.
 * Used by {@link Async#Run} and {@link Scope#run} to accept tasks
 * that may throw.
 */
@FunctionalInterface
public interface RunnableThrowing {
    void run() throws Exception;
}