package megalodonte.application;

import javafx.application.Application;
import javafx.stage.Stage;
import megalodonte.base.theme.FontLoader;

import java.util.function.Consumer;

public final class Bootstrap {
    // Context e Event têm ciclos de vida diferentes — o contexto chega uma vez no startup, e eventos chegam depois, assincronamente.
    public static Consumer<Context> handler;
    public static Consumer<MegalodonteApp.Event> eventHandler;
    public static String appName = null;
    public static String appIconResourcePath = null;

    public static void dispatch(Class<? extends Application> appClass, Stage stage, String[] args) {
        if (appName != null) {
            applyAppName(stage);

            // Garante que exista um .desktop pra rodar direto de JVM (IDE, gradle run,
            // ...) — sem pacote instalado o GNOME/Zorin não tem nada pra casar o
            // WM_CLASS e a dock cai pro ícone genérico do Java. StartupWMClass usa
            // appClass.getName(): é isso, não appName, que o Glass/GTK realmente reporta
            // como WM_CLASS (confirmado via xprop). Ver LinuxDesktopEntry.
            LinuxDesktopEntry.ensure(appName, appIconResourcePath, appClass.getName());
        }

        // Registra fontes de assets/fonts/ (ver FontLoader) antes de qualquer Scene ser
        // criada — é o que torna os nomes de família usáveis em ThemeTypography.fontFamily().
        // No-op silencioso se a app não tiver essa pasta.
        FontLoader.loadAll();

        var context = new Context(stage, args);
        MegalodonteApp.setCurrentContext(context);

        if (handler != null) {
            handler.accept(context);
        }

        stage.show();
    }

    public static void dispatch(Stage stage, String[] args) {
        dispatch(JavaFXHost.class, stage, args);
    }

    public static void dispatch(Stage stage) {
        dispatch(stage, new String[0]);
    }

    private static void applyAppName(Stage stage) {
        // Não influencia o WM_CLASS no Linux (confirmado via xprop — quem determina
        // isso é a classe Application concreta lançada, ver MegalodonteApplication).
        // Mantido porque pode ajudar em outras plataformas/contextos (ex: nome da app
        // no menu do macOS), e é inofensivo no Linux.
        System.setProperty("javafx.application.name", appName);

        try {
            var toolkit = java.awt.Toolkit.getDefaultToolkit();
            var xwmField = toolkit.getClass().getDeclaredField("awtAppClassName");
            xwmField.setAccessible(true);
            xwmField.set(toolkit, appName);
        } catch (Exception ignored) {
            // Não é Linux/GTK, ignora silenciosamente
        }
    }


    public static void dispatchEvent(MegalodonteApp.Event event) {
        if (eventHandler != null) {
            eventHandler.accept(event);
        }
    }
}
