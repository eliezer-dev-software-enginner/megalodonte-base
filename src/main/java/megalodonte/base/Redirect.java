package megalodonte.base;
import java.awt.Desktop;
import java.net.URI;
import java.util.function.Consumer;

final public class Redirect {

    public static void to(String url){
        new Thread(()->{
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

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
