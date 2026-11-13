package megalodonte.application.reload;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Opt-in premain agent used by `megalodonte dev`. File protocol is local to one session. */
public final class ReloadAgent {
    private static volatile boolean active;
    private ReloadAgent() {}

    /** True only when launched with this agent; normal applications do not watch files. */
    public static boolean isActive() { return active; }

    @FunctionalInterface
    interface Change { void run() throws Exception; }

    static final class RollbackFailure extends Exception {
        RollbackFailure(String message, Throwable cause) { super(message, cause); }
    }

    static final class IncompatibleChange extends Exception {
        IncompatibleChange(String message) { super(message); }
        IncompatibleChange(UnsupportedOperationException cause) { super(cause.getMessage(), cause); }
    }

    /** JVM agent entry point. The session directory is private to the CLI invocation. */
    public static void premain(String directory, Instrumentation instrumentation) throws IOException {
        if (directory == null || directory.isBlank()) throw new IllegalArgumentException("Reload session required");
        if (!instrumentation.isRedefineClassesSupported())
            throw new IllegalStateException("This JVM does not support class redefinition");
        Path session = Path.of(directory).toRealPath();
        System.setProperty(RestartWindowState.SESSION_PROPERTY, session.toString());
        Path runtime = session.resolve("active").toRealPath();
        Map<String, byte[]> initial = readTree(runtime);
        active = true;
        Thread.ofPlatform().daemon().name("megalodonte-live-reload").start(() -> watch(session, runtime, initial, instrumentation));
    }

    private static void watch(Path session, Path runtime, Map<String, byte[]> current, Instrumentation instrumentation) {
        String last = "";
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(150);
                Path request = session.resolve("request");
                if (!Files.isRegularFile(request)) continue;
                String id = Files.readString(request).strip();
                if (id.equals(last) || !id.matches("(?:snapshot:)?[0-9]{1,20}")) continue;
                last = id;
                String status;
                try {
                    if (id.startsWith("snapshot:")) {
                        status = "SNAPSHOT\n" + LiveReload.snapshotForRestart() + " windows";
                    } else {
                        Path generation = session.resolve("generations").resolve(id).toRealPath();
                        if (!generation.startsWith(session.resolve("generations"))) throw new IOException("Invalid generation");
                        Map<String, byte[]> next = readTree(generation);
                        status = replace(instrumentation, runtime, current, next);
                        if (status.startsWith("APPLIED")) current = next;
                    }
                } catch (RollbackFailure failure) {
                    status = "FATAL\n" + describe(failure);
                } catch (IncompatibleChange failure) {
                    status = "RESTART_REQUIRED\n" + describe(failure);
                } catch (Throwable failure) {
                    status = "REJECTED\n" + describe(failure);
                }
                Files.writeString(session.resolve("response.tmp"), id + "\n" + status);
                Files.move(session.resolve("response.tmp"), session.resolve("response"), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[live-reload] " + id + " " + status.replace('\n', ' '));
                if (status.startsWith("FATAL")) return;
            } catch (InterruptedException stopped) {
                Thread.currentThread().interrupt();
            } catch (Exception failure) {
                System.err.println("[live-reload] " + describe(failure));
            }
        }
    }

    static String replace(Instrumentation instrumentation, Path runtime,
                          Map<String, byte[]> current, Map<String, byte[]> next) throws Exception {
        // Adding/deleting classes, including synthetic lambda helpers, needs a fresh
        // process in this first protocol. No partially appended, irreversible classpaths.
        if (!classFiles(current).equals(classFiles(next)))
            return "RESTART_REQUIRED\nClass files were added or removed";
        // Load old definitions without initialization before exposing changed files.
        // Otherwise a class first loaded during a failed render could escape rollback.
        for (String file : classFiles(current)) {
            if (!Arrays.equals(current.get(file), next.get(file))) {
                Class.forName(file.substring(0, file.length() - 6).replace('/', '.'), false,
                        ClassLoader.getSystemClassLoader());
            }
        }
        var definitions = new ArrayList<ClassDefinition>();
        var rollback = new ArrayList<ClassDefinition>();
        for (Class<?> type : instrumentation.getAllLoadedClasses()) {
            String file = type.getName().replace('.', '/') + ".class";
            if (!current.containsKey(file) || Arrays.equals(current.get(file), next.get(file))) continue;
            // Never redefine a class from a plugin/dependency that happens to share a name.
            var source = type.getProtectionDomain().getCodeSource();
            if (source == null || !Path.of(source.getLocation().toURI()).toRealPath().equals(runtime))
                throw new IncompatibleChange("Class outside active world: " + type.getName());
            if (!instrumentation.isModifiableClass(type))
                throw new IncompatibleChange("Unmodifiable class: " + type.getName());
            definitions.add(new ClassDefinition(type, next.get(file)));
            rollback.add(new ClassDefinition(type, current.get(file)));
        }
        boolean[] redefined = {false};
        int windows = LiveReload.apply(() -> {
            try {
                instrumentation.redefineClasses(definitions.toArray(ClassDefinition[]::new));
            } catch (UnsupportedOperationException incompatible) {
                throw new IncompatibleChange(incompatible);
            }
            redefined[0] = true;
            syncTree(runtime, current, next);
        }, () -> {
            if (redefined[0]) {
                instrumentation.redefineClasses(rollback.toArray(ClassDefinition[]::new));
                syncTree(runtime, next, current);
            }
        });
        return "APPLIED\n" + definitions.size() + " classes; " + windows + " windows";
    }

    private static Set<String> classFiles(Map<String, byte[]> tree) {
        return tree.keySet().stream().filter(name -> name.endsWith(".class")).collect(Collectors.toSet());
    }

    static Map<String, byte[]> readTree(Path root) throws IOException {
        Map<String, byte[]> result = new HashMap<>();
        try (var paths = Files.walk(root)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                if (!file.toRealPath().startsWith(root)) throw new IOException("Symlink escapes world: " + file);
                result.put(root.relativize(file).toString().replace('\\', '/'), Files.readAllBytes(file));
            }
        }
        return result;
    }

    private static void syncTree(Path root, Map<String, byte[]> old, Map<String, byte[]> next) throws IOException {
        for (String file : old.keySet()) if (!next.containsKey(file)) Files.deleteIfExists(root.resolve(file));
        for (var file : next.entrySet()) {
            if (Arrays.equals(old.get(file.getKey()), file.getValue())) continue;
            Path target = root.resolve(file.getKey());
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(target.getParent(), ".reload-", ".tmp");
            try {
                Files.write(temporary, file.getValue());
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    private static String describe(Throwable error) {
        return error.getClass().getSimpleName() + ": " + String.valueOf(error.getMessage()).replace('\n', ' ');
    }
}
