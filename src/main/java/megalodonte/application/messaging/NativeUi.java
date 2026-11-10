package megalodonte.application.messaging;

import java.util.List;
import java.util.Map;

/** Experimental Java-authored native UI messages. Not a JSONC document API. */
public final class NativeUi {
    private NativeUi() {}
    public enum Kind { COLUMN, ROW, TEXT, BUTTON, INPUT }
    public record Element(String id, Kind kind, String text, List<Element> children) {
        public Element { children = List.copyOf(children); }
    }
    public record Window(String id, String title, double width, double height, Element root) {}
    public record Snapshot(List<Window> windows) {
        public Snapshot { windows = List.copyOf(windows); }
    }
    public record Event(String window, String component, String type, String value) {}
    public record State(Map<String, Map<String, String>> inputs) {
        public State {
            var copy = new java.util.LinkedHashMap<String, Map<String, String>>();
            inputs.forEach((key, value) -> copy.put(key, Map.copyOf(value)));
            inputs = Map.copyOf(copy);
        }
        public String input(String window, String component, String fallback) {
            return inputs.getOrDefault(window, Map.of()).getOrDefault(component, fallback);
        }
    }
    public static Element column(String id, Element... children) {
        return new Element(id, Kind.COLUMN, "", List.of(children));
    }
    public static Element row(String id, Element... children) {
        return new Element(id, Kind.ROW, "", List.of(children));
    }
    public static Element text(String id, String text) { return leaf(id, Kind.TEXT, text); }
    public static Element button(String id, String text) { return leaf(id, Kind.BUTTON, text); }
    public static Element input(String id, String text) { return leaf(id, Kind.INPUT, text); }
    private static Element leaf(String id, Kind kind, String text) {
        return new Element(id, kind, text, List.of());
    }
}
