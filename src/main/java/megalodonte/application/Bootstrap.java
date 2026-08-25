package megalodonte.application;

import javafx.application.Application;
import javafx.stage.Stage;
import megalodonte.base.theme.FontLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public final class Bootstrap {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    // Context and Event have different lifecycles — context arrives once at startup,
    // events arrive later, asynchronously.
    public static Consumer<Context> handler;
    public static Consumer<MegalodonteApp.Event> eventHandler;
    public static String appName = null;
    public static String appIconResourcePath = null;

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

    public static void dispatch(Stage stage, String[] args) {
        dispatch(JavaFXHost.class, stage, args);
    }

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


    public static void dispatchEvent(MegalodonteApp.Event event) {
        log.debug("Dispatching event: {}", event);
        if (eventHandler != null) {
            eventHandler.accept(event);
        } else {
            log.debug("No event handler registered, ignoring event: {}", event);
        }
    }
}
