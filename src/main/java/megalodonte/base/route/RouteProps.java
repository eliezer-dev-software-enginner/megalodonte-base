package megalodonte.base.route;

public record RouteProps(int screenWidth, int screenHeight, String name, boolean screenIsExpandable, String iconPath) {
    public RouteProps(int screenWidth, int screenHeight, String name, boolean screenIsExpandable) {
        this(screenWidth, screenHeight, name, screenIsExpandable, null);
    }
}
