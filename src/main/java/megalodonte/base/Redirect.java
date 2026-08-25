package megalodonte.base;
import java.awt.Desktop;
import java.net.URI;
import java.util.function.Consumer;

/**
 * Opens a URL in the system's default browser. Each call spawns a new thread
 * to avoid blocking the JavaFX Application Thread.
 */
final public class Redirect {

    /**
     * Opens the given URL in the default browser. Logs the error to stderr on failure.
     *
     * @param url the URL to open
     */
    public static void to(String url){
        new Thread(()->{
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Opens the given URL in the default browser, notifying the callback on failure.
     *
     * @param url              the URL to open
     * @param exceptionCallback called with the exception if the browse fails
     */
    public static void to(String url, Consumer<Exception> exceptionCallback){
        new Thread(()->{
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                exceptionCallback.accept(e);
            }
        }).start();
    }
}
