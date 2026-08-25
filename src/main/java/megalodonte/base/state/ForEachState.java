package megalodonte.base.state;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interface for list-based state that maps data items to UI components.
 * Used for reactive list rendering (analogous to React's list rendering).
 *
 * @param <T> the data item type
 * @param <C> the component type
 */
public interface ForEachState<T, C> {
    /** Returns the currently rendered components. */
    List<C> getComponents();

    /** Returns the underlying observable list state. */
    ReadableState<List<T>> getState();

    /**
     * Maps a list of data items to components using the given factory.
     *
     * @param items   the data items
     * @param factory function to create a component from each item
     * @return the list of created components
     */
    static <T, C> List<C> map(List<T> items, Function<T, C> factory) {
        return items.stream().map(factory).collect(Collectors.toList());
    }
}
