package megalodonte.application;

import javafx.application.Application;
import javafx.stage.Stage;
import megalodonte.base.theme.FontLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Internal bootstrap that wires the JavaFX lifecycle to the Megalodonte framework.
 * <p>
 * Called by {@link MegalodonteApplication#start} once JavaFX is ready. Handles:
 * <ol>
 *   <li>App name and {@code .desktop} entry (Linux dev mode)</li>
 *   <li>Custom font loading via {@link FontLoader}</li>
 *   <li>{@link Context} creation and registration</li>
 *   <li>Application handler invocation</li>
 *   <li>Stage show</li>
 * </ol>
 */
public final class Bootstrap {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    // Context and Event have different lifecycles — context arrives once at startup,
    // events arrive later, asynchronously.
    /** Application startup handler, called once with the {@link Context}. */
    public static Consumer<Context> handler;
    /** Framework event handler (e.g. {@link MegalodonteApp.Event#CloseRequest}). */
    public static Consumer<MegalodonteApp.Event> eventHandler;
    /** Application display name, or null if not set. */
    public static String appName = null;
    /** Classpath resource path for the app icon, or null if not set. */
    public static String appIconResourcePath = null;

    /**
     * Main bootstrap entry point. Wires the stage, loads fonts, creates the context,
     * invokes the application handler, and shows the stage.
     *
     * @param appClass the concrete {@link Application} class (determines WM_CLASS on Linux)
     * @param stage    the primary JavaFX stage
     * @param args     command-line arguments
     */
    public static void dispatch(Class<? extends Application> appClass, Stage stage, String[] args) {
        log.info("Bootstrap dispatch starting for {}", appClass.getSimpleName());

        if (appName != null) {
            log.info("Setting application name: '{}'", appName);
            applyAppName(stage);

            // Ensures a .desktop entry exists for running directly from JVM (IDE, gradle run, ...)
            // — without an installed package, GNOME/Zorin has nothing to match the WM_CLASS
            // and the dock falls back to the generic Java icon. StartupWMClass uses
            // appClass.getName(): that, not appName, is what Glass/GTK actually reports
            // as WM_CLASS (confirmed via xprop). See LinuxDesktopEntry.
            LinuxDesktopEntry.ensure(appName, appIconResourcePath, appClass.getName());
        }

        // Registers fonts from assets/fonts/ (see FontLoader) before any Scene is created —
        // this is what makes family names usable in ThemeTypography.fontFamily().
        // Silent no-op if the app doesn't have this directory.
        log.debug("Loading custom fonts from assets/fonts/");
        int fontsLoaded = FontLoader.loadAll();
        log.debug("Font loading complete: {} font(s) registered", fontsLoaded);

        var context = new Context(stage, args);
        MegalodonteApp.setCurrentContext(context);

        if (handler != null) {
            log.debug("Invoking application handler");
            handler.accept(context);
        } else {
            log.warn("No application handler registered");
        }

        stage.show();
        log.info("Bootstrap dispatch complete, stage shown");
    }

    /** @see #dispatch(Class, Stage, String[]) */
    public static void dispatch(Stage stage, String[] args) {
        dispatch(JavaFXHost.class, stage, args);
    }

    /** @see #dispatch(Class, Stage, String[]) */
    public static void dispatch(Stage stage) {
        dispatch(stage, new String[0]);
    }

    private static void applyAppName(Stage stage) {
        // Does not affect WM_CLASS on Linux (confirmed via xprop — what determines
        // that is the concrete Application class launched, see MegalodonteApplication).
        // Kept because it may help on other platforms/contexts (e.g. app name in
        // macOS menu) and is harmless on Linux.
        System.setProperty("javafx.application.name", appName);

        try {
            var toolkit = java.awt.Toolkit.getDefaultToolkit();
            var xwmField = toolkit.getClass().getDeclaredField("awtAppClassName");
            xwmField.setAccessible(true);
            xwmField.set(toolkit, appName);
        } catch (Exception ignored) {
            // Not Linux/GTK, silently ignored
        }
    }


    /**
     * Dispatches a framework event to the registered event handler.
     *
     * @param event the event to dispatch (e.g. {@link MegalodonteApp.Event#CloseRequest})
     */
    public static void dispatchEvent(MegalodonteApp.Event event) {
        log.debug("Dispatching event: {}", event);
        if (eventHandler != null) {
            eventHandler.accept(event);
        } else {
            log.debug("No event handler registered, ignoring event: {}", event);
        }
    }
}
