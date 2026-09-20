package megalodonte.base.route;

public class RouteResolutionException extends RuntimeException {
    public RouteResolutionException(String path, Throwable cause) {
        super("Falha ao resolver rota: " + path, cause);
    }
}