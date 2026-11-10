package megalodonte.application.messaging;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class NativeMessagingIntegrationTest {
    private static <T> T fx(Callable<T> work) throws Exception {
        var task = new FutureTask<>(work); Platform.runLater(task); return task.get(10, TimeUnit.SECONDS);
    }
    private static List<String> command(String mode) {
        return List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("messaging.test.classpath"), NativeMessagingWorkerFixture.class.getName(), mode);
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!fx(condition::getAsBoolean)) {
            if (System.nanoTime() > end) fail("Timed out waiting for native host");
            Thread.sleep(20);
        }
    }
    @Test void keepsTwoNativeWindowsThroughWorkerReplacementFailureAndRecovery() throws Exception {
        var started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        assertTrue(started.await(10, TimeUnit.SECONDS), "A real JavaFX display is required");
        var errors = new CopyOnWriteArrayList<Throwable>();
        var host = fx(() -> new NativeWindowHost(errors::add, Duration.ofSeconds(3)));
        try {
            fx(() -> host.replaceWorker(command("v1"))).get(10, TimeUnit.SECONDS);
            var windows = fx(host::windows);
            var sceneOne = fx(() -> windows.get("one").getScene());
            var sceneTwo = fx(() -> windows.get("two").getScene());
            fx(() -> { windows.get("one").setX(115); windows.get("one").setY(125);
                ((TextField) sceneOne.lookup("#name")).setText("preserved input"); return null; });
            await(host::readyForReplacement);
            fx(() -> { ((Button) sceneOne.lookup("#save")).fire(); return null; });
            await(() -> ((Label) sceneOne.lookup("#version")).getText().equals("v1:1"));
            long first = fx(host::activeGeneration);
            long second = fx(() -> host.replaceWorker(command("v2"))).get(10, TimeUnit.SECONDS);
            assertTrue(second > first);
            fx(() -> {
                assertSame(windows.get("one"), host.windows().get("one"));
                assertSame(windows.get("two"), host.windows().get("two"));
                assertSame(sceneOne, windows.get("one").getScene());
                assertSame(sceneTwo, windows.get("two").getScene());
                assertTrue(windows.values().stream().allMatch(javafx.stage.Stage::isShowing));
                assertEquals(115, windows.get("one").getX(), 2);
                assertEquals("preserved input", ((TextField) sceneOne.lookup("#name")).getText());
                return null;
            });
            for (String invalid : List.of("invalid", "missing", "malformed", "timeout")) {
                var oldRoot = fx(sceneOne::getRoot);
                assertThrows(ExecutionException.class, () -> fx(() -> host.replaceWorker(command(invalid))).get(10, TimeUnit.SECONDS));
                fx(() -> {
                    assertEquals(second, host.activeGeneration());
                    assertSame(oldRoot, sceneOne.getRoot());
                    assertFalse(oldRoot.isDisabled());
                    assertTrue(windows.get("two").isShowing());
                    return null;
                });
            }
            fx(() -> { ((Button) sceneOne.lookup("#crash")).fire(); return null; });
            await(() -> host.activeGeneration() == 0);
            fx(() -> { assertTrue(sceneOne.getRoot().isDisabled()); assertTrue(windows.get("one").isShowing()); return null; });
            fx(() -> host.replaceWorker(command("v3"))).get(10, TimeUnit.SECONDS);
            fx(() -> { assertSame(sceneOne, host.windows().get("one").getScene());
                assertEquals("preserved input", ((TextField) sceneOne.lookup("#name")).getText()); return null; });
            assertTrue(errors.size() >= 5);
        } finally { fx(() -> { host.close(); return null; }); Platform.exit(); }
    }
}
