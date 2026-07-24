package megalodonte.application;

import javafx.application.Application;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MegalodonteApp {

    public enum Event{
        CloseRequest
    }

    private static Context currentContext;

    public static Context getCurrentContext() {
        return currentContext;
    }

    public static String[] getArgs() {
        var ctx = currentContext;
        return ctx != null ? ctx.getArgs() : new String[0];
    }

    static void setCurrentContext(Context context) {
        currentContext = context;
    }

    // Nova API: define o nome da aplicação antes do launch
    public static void appName(String name) {
        Bootstrap.appName = name;
    }

    /**
     * Classpath resource path (e.g. {@code "/assets/app_ico.png"}) for the app's icon.
     * Used on Linux to seed the dev-mode {@code .desktop} entry — see
     * {@link LinuxDesktopEntry}. Gets the dock/taskbar icon working when running
     * straight from a JVM (IDE, {@code gradle run}, ...) instead of an installed
     * package. Pair with a custom launcher class (see the {@code run(Class, ...)}
     * overloads below) to also get a WM_CLASS unique to your app.
     */
    public static void appIcon(String classpathResourcePath) {
        Bootstrap.appIconResourcePath = classpathResourcePath;
    }

    public static void run(Consumer<Context> contextHandler, Consumer<Event> onEvent) {
        Bootstrap.handler = contextHandler;
        Bootstrap.eventHandler = onEvent;
        Application.launch(JavaFXHost.class);
    }

    public static void run(Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(JavaFXHost.class);
    }

    public static void run(String[] args, Consumer<Context> contextHandler, Consumer<Event> onEvent) {
        Bootstrap.handler = contextHandler;
        Bootstrap.eventHandler = onEvent;
        Application.launch(JavaFXHost.class, args);
    }

    public static void run(String[] args, Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(JavaFXHost.class, args);
    }

    /**
     * Same as {@link #run(String[], Consumer, Consumer)}, but launching through
     * {@code appClass} instead of the shared default {@link JavaFXHost}.
     * <p>
     * On Linux/GTK, the running window's WM_CLASS is the launched
     * {@link Application} subclass's fully-qualified name (confirmed via
     * {@code xprop WM_CLASS} — {@code appName(String)} alone does not affect it).
     * Every app defaulting to {@link JavaFXHost} therefore reports the same
     * WM_CLASS, and GNOME's dock/taskbar can't tell them apart (same icon, same
     * grouping). Pass your own {@link MegalodonteApplication} subclass here to get
     * a WM_CLASS — and taskbar icon — unique to your app:
     * <pre>{@code
     * public static class AppHost extends MegalodonteApplication {}
     *
     * static void main(String[] args) {
     *     MegalodonteApp.run(AppHost.class, args, Main::start, Main::onEvent);
     * }
     * }</pre>
     */
    public static void run(Class<? extends Application> appClass, String[] args,
                            Consumer<Context> contextHandler, Consumer<Event> onEvent) {
        Bootstrap.handler = contextHandler;
        Bootstrap.eventHandler = onEvent;
        Application.launch(appClass, args);
    }

    public static void run(Class<? extends Application> appClass, String[] args, Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(appClass, args);
    }

    public static void run(Class<? extends Application> appClass, Consumer<Context> contextHandler, Consumer<Event> onEvent) {
        Bootstrap.handler = contextHandler;
        Bootstrap.eventHandler = onEvent;
        Application.launch(appClass);
    }

    public static void run(Class<? extends Application> appClass, Consumer<Context> contextHandler) {
        Bootstrap.handler = contextHandler;
        Application.launch(appClass);
    }
}
