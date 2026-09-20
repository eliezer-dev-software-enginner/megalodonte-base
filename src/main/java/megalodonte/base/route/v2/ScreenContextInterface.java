package megalodonte.base.route.v2;

import javafx.scene.Scene;
import javafx.stage.Stage;
import megalodonte.base.async.RunnableThrowing;
import megalodonte.base.async.Scope;
import megalodonte.base.route.RouteNotFoundException;
import megalodonte.base.route.RouteProps;
import megalodonte.base.route.RouterBase;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Screen-level context provided to each screen. Exposes navigation, route
 * parameters, scope and window spawning. Implemented by {@link ScreenContextBase}.
 */
public interface ScreenContextInterface {

    /**
     * Escopo de cancelamento vinculado a esta navegação: cancelado automaticamente pelo
     * {@link RouterBase} assim que a tela associada é destruída (antes de {@code onDestroy()}
     * rodar). Trabalho assíncrono iniciado via {@code scope().run(...)} não fica pendurado
     * depois que o usuário navega pra outro lugar.
     */
    Scope scope();

    /**
     * Navigates to the given route path within this screen's stage.
     *
     * <p>Mutates the existing {@link Scene} root instead of creating a new Scene,
     * preserving window listeners and state. After navigation, applies the destination
     * route's {@link RouteProps} (dimensions, title, resizability) to the stage.</p>
     *
     * <p>If no Scene exists yet on the stage, a new one is created with the route's dimensions.</p>
     *
     * @param path the route identification to navigate to (e.g. "home", "user/${id}")
     * @throws RouteNotFoundException if no route matches the given path
     */
     void navigate(String path);

    /**
     * Fecha todas as janelas spawned (inclusive a atual, se for uma spawned)
     * e navega a stage principal para o path informado. Uso: logout/"Sair",
     * garantindo retorno limpo à Auth independente de onde foi clicado.
     */
    void navigateAndCloseOthers(String path);

    /**
     * Executa o callback quando a Scene estiver pronta na Stage.
     * Cobre tanto o caso onde ela ainda não existe (aguarda) quanto
     * o caso onde já está disponível (executa imediatamente).
     */
     void whenReady(RunnableThrowing callback);

    /**
     * Get current JavaFX scene of this screen
     * @return {@link Scene}
     */
    Scene getJavaFXScene();

    /**
     * Route parameters for the current navigation, resolved from dynamic segments
     * ({@code ${param}}) — e.g. for path {@code "product/${id}"}: {@code { "id" = "123" }}.
     */
    Map<String, String> getParams();

    /**
     * The JavaFX {@link Stage} this screen is currently attached to.
     */
    Stage selfStage();

    /**
     * Spawns a new window for the given route.
     *
     * @param path route identification to spawn
     */
    void spawnWindow(String path);

    /**
     * Spawns a new window for the given route.
     *
     * @param path route identification to spawn
     * @param errorHandler callback invoked if spawning fails
     */
    void spawnWindow(String path, Consumer<Exception> errorHandler);
}