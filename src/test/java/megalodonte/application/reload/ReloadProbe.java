package megalodonte.application.reload;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.Route;
import megalodonte.base.route.RouteProps;
import megalodonte.base.route.RouteTable;
import megalodonte.base.route.ScreenManager;
import megalodonte.base.state.State;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Child JVM fixture: two independent windows and stateful screen instances. */
public final class ReloadProbe {
    public static void main(String[] args) throws Exception {
        Path session = Path.of(args[0]);
        List<Stage> windows = new ArrayList<>();
        List<ScreenComponent> screens = new ArrayList<>();
        List<Scene> scenes = new ArrayList<>();
        Platform.startup(() -> {
            try {
                for (int i = 0; i < 2; i++) {
                    var screen = (ScreenComponent) Class.forName("fixture.SampleScreen").getConstructor().newInstance();
                    screen.getClass().getField("count").set(screen, 40 + i);
                    Stage stage = new Stage();
                    Route route = new Route("fixture", context -> screen, new RouteProps(240, 100, "Reload test", true));
                    var manager = new ScreenManager(new RouteTable(Set.of(route), "fixture"));
                    Parent root = (Parent) manager.mount(stage, route, Map.of(), null).view().getJavaFxNode();
                    stage.setScene(new Scene(root, 240, 100));
                    stage.show();
                    windows.add(stage);
                    screens.add(screen);
                    scenes.add(stage.getScene());
                }
                // A framework-managed secondary stage remains reloadable through
                // its explicit binding; refresh must not depend on JavaFX's global
                // observable window list at the instant a generation is applied.
                windows.get(1).hide();
                Files.writeString(session.resolve("ready"), "ready");
            } catch (Throwable error) {
                error.printStackTrace();
                System.exit(1);
            }
        });
        String last = "";
        while (true) {
            Thread.sleep(50);
            Path inspect = session.resolve("inspect");
            if (!Files.exists(inspect)) continue;
            String id = Files.readString(inspect);
            if (id.equals(last)) continue;
            last = id;
            Platform.runLater(() -> {
                try {
                    var result = new StringBuilder(id);
                    for (int i = 0; i < windows.size(); i++) {
                        if (windows.get(i).getScene() != scenes.get(i)) throw new AssertionError("Scene was replaced");
                        var screen = screens.get(i);
                        int before = screen.getClass().getField("notifications").getInt(screen);
                        @SuppressWarnings("unchecked")
                        State<Integer> state = (State<Integer>) screen.getClass().getField("state").get(screen);
                        state.set(state.get() + 1);
                        if (screen.getClass().getField("notifications").getInt(screen) - before != 1)
                            throw new AssertionError("Leaked or lost render subscriptions");
                        result.append("\n").append(((Label) windows.get(i).getScene().getRoot().getChildrenUnmodifiable().get(0)).getText());
                        result.append(":mounts=").append(screens.get(i).getClass().getField("mounts").get(screens.get(i)));
                    }
                    Files.writeString(session.resolve("inspection.tmp"), result);
                    Files.move(session.resolve("inspection.tmp"), session.resolve("inspection"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception error) { throw new RuntimeException(error); }
            });
        }
    }
}
