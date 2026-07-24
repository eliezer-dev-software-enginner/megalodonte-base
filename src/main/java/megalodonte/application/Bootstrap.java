package megalodonte.application;

import javafx.stage.Stage;
import megalodonte.base.theme.FontLoader;

import java.util.function.Consumer;

public final class Bootstrap {
    // Context e Event têm ciclos de vida diferentes — o contexto chega uma vez no startup, e eventos chegam depois, assincronamente.
    public static Consumer<Context> handler;
    public static Consumer<MegalodonteApp.Event> eventHandler;
    public static String appName = null;

    public static void dispatch(Stage stage, String[] args) {
        // Define WM_CLASS no Linux antes de qualquer coisa
        if (appName != null) {
            applyAppName(stage);
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

    public static void dispatch(Stage stage) {
        dispatch(stage, new String[0]);
    }

    private static void applyAppName(Stage stage) {
        // Seta o título da aplicação AWT — isso influencia o WM_CLASS no Linux
        System.setProperty("javafx.application.name", appName);

        // Tenta setar via toolkit GTK (Linux)
        try {
            var toolkit = java.awt.Toolkit.getDefaultToolkit();
            // Força o nome da app no AWT toolkit antes da janela aparecer
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