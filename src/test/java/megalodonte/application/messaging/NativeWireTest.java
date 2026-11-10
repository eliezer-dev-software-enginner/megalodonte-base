package megalodonte.application.messaging;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NativeWireTest {
    private static NativeWire reader(String text) {
        return new NativeWire(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), new ByteArrayOutputStream());
    }
    @Test void roundTripsUnicodeStateAndRejectsRepeatedSequence() throws Exception {
        var out = new ByteArrayOutputStream();
        new NativeWire(InputStream.nullInputStream(), out).send(7, "HELLO", new NativeUi.State(Map.of("window", Map.of("name", "ação\n✓"))));
        String frame = out.toString(StandardCharsets.UTF_8);
        var wire = reader(frame + frame);
        var message = wire.read();
        assertEquals(7, message.generation());
        assertEquals("ação\n✓", message.body(NativeUi.State.class).input("window", "name", ""));
        assertThrows(IOException.class, wire::read);
    }
    @Test void rejectsMalformedUnsupportedTruncatedAndOversizedFrames() {
        for (String text : new String[]{"not json\n", "null\n", "{}\n", "{\"version\":2}\n", "{", "x".repeat(NativeWire.MAX_FRAME + 1)})
            assertThrows(IOException.class, () -> reader(text).read());
    }
}
