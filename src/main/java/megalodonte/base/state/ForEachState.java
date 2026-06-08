package megalodonte.base.state;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ForEachState<T, C> {
    List<C> getComponents();
    ReadableState<List<T>> getState();

    static <T, C> List<C> map(List<T> items, Function<T, C> factory) {
        return items.stream().map(factory).collect(Collectors.toList());
    }
}
