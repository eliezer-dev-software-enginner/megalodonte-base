package megalodonte.base;
import java.awt.Desktop;
import java.net.URI;

final public class Redirect {

    public static void to(String url){
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
