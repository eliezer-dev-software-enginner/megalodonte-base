package megalodonte.base.async;

@FunctionalInterface
public interface RunnableThrowing {
    void run() throws Exception;
}