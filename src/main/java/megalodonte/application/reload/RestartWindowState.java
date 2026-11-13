package megalodonte.application.reload;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Session-local window metadata used only when a development child JVM restarts. */
final class RestartWindowState {
    static final String SESSION_PROPERTY = "megalodonte.live.session";
    private static final String FILE = "window-state.json";

    record Window(String id, double x, double y, double width, double height,
                  boolean maximized, boolean fullScreen, boolean iconified, boolean showing) {}
    record Snapshot(boolean applicationFocused, String focusedWindow, Map<String, Window> windows) {}

    private RestartWindowState() {}

    static Path file() {
        String session = System.getProperty(SESSION_PROPERTY);
        return session == null || session.isBlank() ? null : Path.of(session).resolve(FILE);
    }

    static void write(Snapshot snapshot) throws IOException {
        Path target = file();
        if (target == null) return;
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        root.addProperty("applicationFocused", snapshot.applicationFocused());
        if (snapshot.focusedWindow() != null) root.addProperty("focusedWindow", snapshot.focusedWindow());
        JsonArray windows = new JsonArray();
        snapshot.windows().values().forEach(window -> {
            JsonObject item = new JsonObject();
            item.addProperty("id", window.id());
            item.addProperty("x", window.x());
            item.addProperty("y", window.y());
            item.addProperty("width", window.width());
            item.addProperty("height", window.height());
            item.addProperty("maximized", window.maximized());
            item.addProperty("fullScreen", window.fullScreen());
            item.addProperty("iconified", window.iconified());
            item.addProperty("showing", window.showing());
            windows.add(item);
        });
        root.add("windows", windows);
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".window-state-", ".tmp");
        try {
            Files.writeString(temporary, root.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static Snapshot read() {
        Path target = file();
        if (target == null || !Files.isRegularFile(target)) return null;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(target, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("version") || root.get("version").getAsInt() != 1) return null;
            Map<String, Window> windows = new LinkedHashMap<>();
            for (var element : root.getAsJsonArray("windows")) {
                JsonObject item = element.getAsJsonObject();
                Window window = new Window(item.get("id").getAsString(), item.get("x").getAsDouble(),
                        item.get("y").getAsDouble(), item.get("width").getAsDouble(),
                        item.get("height").getAsDouble(), item.get("maximized").getAsBoolean(),
                        item.get("fullScreen").getAsBoolean(), item.get("iconified").getAsBoolean(),
                        item.get("showing").getAsBoolean());
                if (valid(window)) windows.put(window.id(), window);
            }
            String focusedWindow = root.has("focusedWindow") ? root.get("focusedWindow").getAsString() : null;
            return new Snapshot(root.get("applicationFocused").getAsBoolean(), focusedWindow, windows);
        } catch (RuntimeException | IOException invalid) {
            ReloadConsole.error("Ignoring invalid window restart state: " + invalid.getMessage());
            return null;
        }
    }

    static void restore(Stage stage, Window window) {
        if (window == null) return;
        Rectangle2D bounds = visibleBounds(window);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(window.maximized());
        stage.setFullScreen(window.fullScreen());
        stage.setIconified(window.iconified());
    }

    private static boolean valid(Window window) {
        return window.id() != null && !window.id().isBlank()
                && Double.isFinite(window.x()) && Double.isFinite(window.y())
                && Double.isFinite(window.width()) && Double.isFinite(window.height())
                && window.width() > 0 && window.height() > 0;
    }

    /** Keep a restored window reachable when monitors were disconnected between restarts. */
    private static Rectangle2D visibleBounds(Window window) {
        Rectangle2D requested = new Rectangle2D(window.x(), window.y(), window.width(), window.height());
        boolean visible = Screen.getScreens().stream().map(Screen::getVisualBounds)
                .anyMatch(screen -> screen.intersects(requested));
        if (visible) return requested;
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double width = Math.min(window.width(), screen.getWidth());
        double height = Math.min(window.height(), screen.getHeight());
        return new Rectangle2D(screen.getMinX() + (screen.getWidth() - width) / 2,
                screen.getMinY() + (screen.getHeight() - height) / 2, width, height);
    }
}
