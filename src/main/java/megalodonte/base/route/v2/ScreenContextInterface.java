package megalodonte.base.route.v2;

import javafx.scene.Scene;
import megalodonte.base.async.Scope;
import megalodonte.base.route.RouteNotFoundException;
import megalodonte.base.route.RouteProps;
import megalodonte.base.route.RouterBase;

import java.util.function.Consumer;

/**
 * Marker interface for screen-level context objects provided by the router.
 * Concrete implementations (in {@code megalodonte-router}) expose navigation,
 * route parameters, and scope to individual screens.
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
     void whenReady(Consumer<ScreenContextInterface> callback);

    /**
     * Get current JavaFX scene of this screen
     * @return {@link Scene}
     */
    Scene getJavaFXScene();
}