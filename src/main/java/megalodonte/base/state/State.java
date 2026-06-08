package megalodonte.base.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class State<T> implements ReadableState<T> {

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public State(T initial) {
        this.value = initial;
    }

    public static <T> State<T> of(T initial) {
        return new State<>(initial);
    }

    public T get() {
        return value;
    }

    public T getOrDefault(T defaultValue) {
        return value == null? defaultValue : value;
    }


    @Override
    public boolean isNull() {
        return get() == null;
    }

    public void set(T newValue) {
        if (Objects.equals(this.value, newValue)) {
            return;
        }

        this.value = newValue;

        for (var listener : List.copyOf(listeners)) {
            listener.accept(value);
        }
    }

    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
    }
}
