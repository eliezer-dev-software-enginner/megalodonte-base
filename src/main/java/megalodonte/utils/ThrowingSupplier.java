package megalodonte.utils;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}