package megalodonte.base.state;

public interface ReadableState<T> {
    T get();
    boolean isNull();
    void subscribe(java.util.function.Consumer<T> listener);

    default <R> ReadableState<R> map(java.util.function.Function<T, R> mapper) {
        State<R> derived = new State<>(mapper.apply(get()));

        subscribe(value -> derived.set(mapper.apply(value)));

        return derived;
    }
}
