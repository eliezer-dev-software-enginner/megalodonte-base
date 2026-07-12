package megalodonte.application;

public final class ErrorReporter {

    @FunctionalInterface
    public interface Handler {
        void onError(Throwable t);
    }

    private static Handler handler = ErrorReporter::defaultHandler;

    private ErrorReporter() {}

    /** Chamado pelo app no bootstrap para plugar a UI de erro específica. */
    public static void register(Handler appHandler) {
        handler = appHandler;
    }

    public static void handle(Throwable t) {
        log(t);
        handler.onError(t);
    }

    private static void defaultHandler(Throwable t) {
        // fallback caso o app não registre nada — nunca deve quebrar silenciosamente
        System.err.println("[ErrorReporter] Nenhum handler registrado. Erro: " + t.getMessage());
        t.printStackTrace();
    }

    private static void log(Throwable t) {
        // gravação em arquivo, mesmo padrão do log de close que já existe no Main
    }
}