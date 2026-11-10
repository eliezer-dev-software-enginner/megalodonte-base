package megalodonte.application.reload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReloadIntegrationTest {
    @TempDir Path session;

    private void compile(Path output, String body, String fields) throws Exception {
        Path source = session.resolve("source/fixture/SampleScreen.java");
        Files.createDirectories(source.getParent());
        Files.createDirectories(output);
        Files.writeString(source, """
                package fixture;
                import megalodonte.base.components.Component;
                import megalodonte.base.components.ScreenComponent;
                import javafx.scene.control.Label;
                import javafx.scene.layout.VBox;
                public class SampleScreen implements ScreenComponent {
                    public int count = 0;
                    public int mounts = 0;
                    public int notifications = 0;
                    public final megalodonte.base.state.State<Integer> state = megalodonte.base.state.State.of(0);
                    %s
                    public void onMount() { mounts++; }
                    public Component render() { state.subscribe(value -> notifications++); %s }
                }
                """.formatted(fields, body));
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-classpath", System.getProperty("reload.test.classpath"), "-d", output.toString(), source.toString()));
    }

    private String await(Path file, String prefix, Process child) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.exists(file)) {
                String value = Files.readString(file);
                if (value.startsWith(prefix)) return value;
            }
            assertTrue(child.isAlive(), () -> "Child exited: " + readLog());
            Thread.sleep(50);
        }
        fail("Timed out waiting for " + file + ": " + readLog());
        return "";
    }

    private String readLog() {
        try { return Files.readString(session.resolve("child.log")); }
        catch (Exception ignored) { return "No log"; }
    }

    private String apply(int id, String body, String fields, Process child) throws Exception {
        compile(session.resolve("generations/" + id), body, fields);
        Files.writeString(session.resolve("request"), String.valueOf(id));
        return await(session.resolve("response"), id + "\n", child);
    }

    private String inspect(int id, Process child) throws Exception {
        Files.writeString(session.resolve("inspect"), String.valueOf(id));
        return await(session.resolve("inspection"), id + "\n", child);
    }

    @Test void replacesBytecodeInTwoWindowsPreservesStateAndRollsBackFailedRender() throws Exception {
        // Intentionally fails (rather than silently skipping) when the selected
        // integration-test environment lacks JavaFX/display support.
        String original = "return Component.CreateFromJavaFxNode(new VBox(new Label(\"before:\" + count)));";
        String changed = "return Component.CreateFromJavaFxNode(new VBox(new Label(\"after:\" + count)));";
        compile(session.resolve("active"), original, "");
        String cp = session.resolve("active") + File.pathSeparator + System.getProperty("reload.test.classpath");
        Process child = new ProcessBuilder(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "--enable-native-access=ALL-UNNAMED", "-Dprism.order=sw",
                "-javaagent:" + System.getProperty("reload.test.agent") + "=" + session,
                "-cp", cp, ReloadProbe.class.getName(), session.toString()))
                .redirectErrorStream(true).redirectOutput(session.resolve("child.log").toFile()).start();
        try {
            await(session.resolve("ready"), "ready", child);
            assertEquals("1\nbefore:40:mounts=1\nbefore:41:mounts=1", inspect(1, child));
            assertTrue(apply(1, changed, "", child).contains("APPLIED\n1 classes; 2 windows"));
            assertEquals("2\nafter:40:mounts=1\nafter:41:mounts=1", inspect(2, child));
            // First window prepares successfully; second fails. Neither root commits.
            assertTrue(apply(2, "if (count == 41) throw new UnsupportedOperationException(\"bad render\"); " + original, "", child).contains("REJECTED"));
            assertEquals("3\nafter:40:mounts=1\nafter:41:mounts=1", inspect(3, child));
            assertTrue(apply(3, original, "public int newField;", child).contains("RESTART_REQUIRED"));
            assertEquals("4\nafter:40:mounts=1\nafter:41:mounts=1", inspect(4, child));
            assertTrue(apply(4, original, "", child).contains("APPLIED"));
            assertEquals("5\nbefore:40:mounts=1\nbefore:41:mounts=1", inspect(5, child));
        } finally {
            child.destroy();
            if (!child.waitFor(5, TimeUnit.SECONDS)) child.destroyForcibly().waitFor();
        }
    }
}
