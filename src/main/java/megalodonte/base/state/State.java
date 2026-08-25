package megalodonte.base.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Observable mutable state container. Holds a value and notifies subscribers
 * on change (skip-set semantics: equal values are not propagated).
 * <p>
 * Subscribers are called immediately upon subscription with the current value.
 *
 * @param <T> the type of the held value
 */
public class State<T> implements ReadableState<T> {

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    /** Creates a new state with the given initial value. */
    public State(T initial) {
        this.value = initial;
    }

    /** Factory method for creating a new state. */
    public static <T> State<T> of(T initial) {
        return new State<>(initial);
    }

    /** Returns the current value. */
    public T get() {
        return value;
    }

    /** Returns the current value, or {@code defaultValue} if null. */
    public T getOrDefault(T defaultValue) {
        return value == null? defaultValue : value;
    }


    @Override
    public boolean isNull() {
        return get() == null;
    }

    /**
     * Sets the value and notifies subscribers if the value changed.
     * Uses {@link Objects#equals} for comparison.
     *
     * @param newValue the new value
     */
    public void set(T newValue) {
        if (Objects.equals(this.value, newValue)) {
            return;
        }

        this.value = newValue;

        for (var listener : List.copyOf(listeners)) {
            listener.accept(value);
        }
    }

    /**
     * Subscribes a listener to value changes. Called immediately with the
     * current value, then again on every subsequent {@link #set} that
     * produces a different value.
     *
     * @param listener called with each new value
     */
    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
    }
}
