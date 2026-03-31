package megalodonte.base;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Usage example
 *
 *   new KeyBind()
 *         .on(KeyCode.DIGIT1, () -> showPopup("Atalho SHIFT+1!"), KeyBind.Modifier.SHIFT)
 *         .on(KeyCode.S,      () -> salvar(),                     KeyBind.Modifier.CTRL)
 *         .on(KeyCode.F5,     () -> recarregar())
 *         .attach(screenContext.selfStage().getScene());
 */
public final class KeyBind {

    public enum Modifier { SHIFT, CTRL, ALT }

    private record Combo(KeyCode key, Set<Modifier> modifiers) {}

    private final Map<Combo, Runnable> bindings = new HashMap<>();

    public KeyBind on(KeyCode key, Runnable action, Modifier... modifiers) {
        var combo = new Combo(key, Set.of(modifiers));
        bindings.put(combo, action);
        return this;
    }

    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handle);
    }

    public void attach(Node node) {
        node.addEventFilter(KeyEvent.KEY_PRESSED, this::handle);
    }

    private void handle(KeyEvent e) {
        Set<Modifier> pressed = activeModifiers(e);
        var combo = new Combo(e.getCode(), pressed);
        Runnable action = bindings.get(combo);
        if (action != null) action.run();
    }

    private Set<Modifier> activeModifiers(KeyEvent e) {
        return Arrays.stream(Modifier.values())
                .filter(m -> switch (m) {
                    case SHIFT -> e.isShiftDown();
                    case CTRL  -> e.isControlDown();
                    case ALT   -> e.isAltDown();
                })
                .collect(Collectors.toSet());
    }
}