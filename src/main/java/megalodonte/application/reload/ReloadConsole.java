package megalodonte.application.reload;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;

/** Jansi-backed development diagnostics; regular application logging remains on SLF4J. */
final class ReloadConsole {
    private static boolean installed;

    private ReloadConsole() {}

    static synchronized void install() {
        if (installed || System.getenv().containsKey("NO_COLOR")) return;
        AnsiConsole.systemInstall();
        installed = true;
    }

    static void status(String message, String status) {
        Ansi.Color color = switch (status) {
            case "APPLIED", "SNAPSHOT" -> Ansi.Color.GREEN;
            case "RESTART_REQUIRED" -> Ansi.Color.YELLOW;
            case "REJECTED", "FAILED", "FATAL" -> Ansi.Color.RED;
            default -> Ansi.Color.CYAN;
        };
        write(AnsiConsole.out(), message, color);
    }

    static void error(String message) {
        write(AnsiConsole.err(), message, Ansi.Color.RED);
    }

    private static void write(PrintStream stream, String message, Ansi.Color color) {
        if (!installed) {
            stream.println("[live-reload] " + message);
            return;
        }
        stream.println(Ansi.ansi().fgCyan().a("[live-reload]").reset().a(" ")
                .fg(color).a(message).reset());
    }
}
