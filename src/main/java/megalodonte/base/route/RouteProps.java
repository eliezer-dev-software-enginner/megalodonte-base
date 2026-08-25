package megalodonte.base.route;

/**
 * Immutable properties for a route. Defines the screen dimensions, title,
 * resizability, and optional icon path.
 *
 * @param screenWidth         initial screen width (before scaling)
 * @param screenHeight        initial screen height (before scaling)
 * @param name                screen title displayed in the title bar
 * @param screenIsExpandable  whether the user can resize the window
 * @param iconPath            classpath resource path for the window icon, or null
 */
public record RouteProps(int screenWidth, int screenHeight, String name, boolean screenIsExpandable, String iconPath) {
    public RouteProps(int screenWidth, int screenHeight, String name, boolean screenIsExpandable) {
        this(screenWidth, screenHeight, name, screenIsExpandable, null);
    }
}
