package megalodonte.application;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Base class for an app's own JavaFX entry point.
 * <p>
 * On Linux/GTK, Glass reports the running window's WM_CLASS as the concrete
 * {@link Application} subclass's fully-qualified name (confirmed empirically via
 * {@code xprop WM_CLASS} — {@code javafx.application.name}/{@link MegalodonteApp#appName}
 * does <em>not</em> affect this, despite what the name suggests). If every app built
 * on this framework launched through the same shared class ({@link JavaFXHost}),
 * they'd all report the same WM_CLASS, and GNOME-based desktop environments (Zorin
 * OS included) would treat them as the same app for dock/taskbar purposes — same
 * icon, same grouping, regardless of which one is actually running.
 * <p>
 * Extend this with an empty subclass in your own app and pass it to
 * {@link MegalodonteApp#run(Class, String[], java.util.function.Consumer, java.util.function.Consumer)}
 * to get a WM_CLASS — and therefore a distinct taskbar icon — of your own:
 * <pre>{@code
 * public class Main {
 *     public static class AppHost extends MegalodonteApplication {}
 *
 *     static void main(String[] args) {
 *         MegalodonteApp.appName("My App");
 *         MegalodonteApp.appIcon("/assets/app_icon.png");
 *         MegalodonteApp.run(AppHost.class, args, Main::start, Main::onEvent);
 *     }
 * }
 * }</pre>
 */
public abstract class MegalodonteApplication extends Application {
    @Override
    public final void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(e -> Bootstrap.dispatchEvent(MegalodonteApp.Event.CloseRequest));
        var raw = getParameters().getRaw();
        Bootstrap.dispatch(getClass(), primaryStage, raw.toArray(new String[0]));
    }
}
