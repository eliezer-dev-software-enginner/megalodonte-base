package megalodonte.utils;

/**
 * A {@link java.util.function.Supplier} variant that allows checked exceptions.
 *
 * @param <T> the return type
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}