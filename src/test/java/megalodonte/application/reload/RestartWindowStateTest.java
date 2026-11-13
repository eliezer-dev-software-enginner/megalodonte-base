package megalodonte.application.reload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class RestartWindowStateTest {
    @TempDir Path session;

    @Test
    void roundTripsWindowGeometryModeVisibilityAndFocus() throws Exception {
        String previous = System.getProperty(RestartWindowState.SESSION_PROPERTY);
        try {
            System.setProperty(RestartWindowState.SESSION_PROPERTY, session.toString());
            var windows = new LinkedHashMap<String, RestartWindowState.Window>();
            windows.put("primary", new RestartWindowState.Window(
                    "primary", 120, 80, 1100, 720, true, false, false, true));
            windows.put("window-1", new RestartWindowState.Window(
                    "window-1", 300, 220, 640, 480, false, false, true, true));

            RestartWindowState.write(new RestartWindowState.Snapshot(true, "window-1", windows));
            var restored = RestartWindowState.read();

            assertNotNull(restored);
            assertTrue(restored.applicationFocused());
            assertEquals("window-1", restored.focusedWindow());
            assertEquals(windows, restored.windows());
            assertTrue(Files.readString(session.resolve("window-state.json")).contains("\"version\":1"));
        } finally {
            if (previous == null) System.clearProperty(RestartWindowState.SESSION_PROPERTY);
            else System.setProperty(RestartWindowState.SESSION_PROPERTY, previous);
        }
    }

    @Test
    void ignoresMalformedSessionState() throws Exception {
        String previous = System.getProperty(RestartWindowState.SESSION_PROPERTY);
        try {
            System.setProperty(RestartWindowState.SESSION_PROPERTY, session.toString());
            Files.writeString(session.resolve("window-state.json"), "not json");
            assertNull(RestartWindowState.read());
        } finally {
            if (previous == null) System.clearProperty(RestartWindowState.SESSION_PROPERTY);
            else System.setProperty(RestartWindowState.SESSION_PROPERTY, previous);
        }
    }
}
