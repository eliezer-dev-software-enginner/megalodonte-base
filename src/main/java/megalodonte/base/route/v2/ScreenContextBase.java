package megalodonte.base.route.v2;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import megalodonte.application.ErrorReporter;
import megalodonte.base.async.RunnableThrowing;
import megalodonte.base.async.Scope;
import megalodonte.base.route.RouteProps;
import megalodonte.base.route.RouteResult;
import megalodonte.base.route.RouterBase;
import megalodonte.base.scale.ScaleProvider;
import megalodonte.base.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Marker interface for screen-level context objects provided by the router.
 * Concrete implementations (in {@code megalodonte-router}) expose navigation,
 * route parameters, and scope to individual screens.
 */
public abstract class ScreenContextBase implements ScreenContextInterface {
    private final Stage selfStage;
    private final RouterBase router;
    private final Scope scope;

    private static final Duration TRANSITION_DURATION = Duration.millis(200);
    private Map<String, String> params;

    private static final Logger log = LoggerFactory.getLogger(ScreenContextBase.class);

    public ScreenContextBase(Stage selfStage, RouterBase router){
        this.selfStage = selfStage;
        this.router = router;
        this.scope = new Scope();
    }

    @Override
    public Scope scope() {
        return scope;
    }

    @Override
    public void navigate(String path) {
        log.debug("Navigating to '{}' within current stage", path);
        RouteResult result = router.navigateOnStage(path, selfStage);
        applyRouteResult(result, selfStage);
    }

    @Override
    public void navigateAndCloseOthers(String path) {
        log.info("Navigating to '{}' and closing all spawned windows", path);
        RouteResult result = router.navigateAndCloseOthers(path);
        applyRouteResult(result, router.mainStage());
    }

    private void applyRouteResult(RouteResult result, Stage targetStage) {
        RouteProps props = result.props();
        Parent newRoot = (Parent) result.view().getJavaFxNode();

        Scene current = targetStage.getScene();
        if (current == null) {
            log.debug("No scene found, creating new scene with {}x{}", props.screenWidth(), props.screenHeight());
            Scene newScene = new Scene(newRoot,
                    ScaleProvider.scale(props.screenWidth()),
                    ScaleProvider.scale(props.screenHeight()));
            ThemeManager.applyFontFamily(newScene);
            targetStage.setScene(newScene);
            applyStageProps(targetStage, props);
            return;
        }

        log.debug("Applying fade transition to swap scene root");
        Parent oldRoot = current.getRoot();

        FadeTransition fadeOut = new FadeTransition(TRANSITION_DURATION, oldRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            newRoot.setOpacity(0.0);
            current.setRoot(newRoot);
            applyStageProps(targetStage, props);

            FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION, newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void applyStageProps(Stage stage, RouteProps props) {
        double width = ScaleProvider.scale(props.screenWidth());
        double height = ScaleProvider.scale(props.screenHeight());

        stage.setWidth(width);
        stage.setHeight(height);
        // NÃO trava setMaxWidth/setMaxHeight aqui (já foi tentado — ver DECISIONS.md do app
        // consumidor). Um teto de tamanho vale pra qualquer resize, inclusive maximizar: numa
        // rota resizable=true, isso capava a janela no tamanho declarado da rota mesmo tentando
        // maximizar de propósito, o que contradiz "screenIsExpandable" — o usuário não consegue
        // mais aproveitar a tela toda. O problema original (conteúdo empurrando a janela além do
        // declarado sozinho) já tem correção no nível certo, no app consumidor: o conteúdo em si
        // precisa respeitar o espaço disponível e rolar por dentro (Show.fillHeight + ScrollPane)
        // em vez da janela ser impedida de crescer.
        if (props.name() != null) {
            stage.setTitle(props.name());
        }
        if (props.iconPath() != null && !props.iconPath().isEmpty()) {
            stage.getIcons().add(new Image(props.iconPath()));
        }
        stage.setResizable(props.screenIsExpandable());
        stage.centerOnScreen();
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Stage selfStage(){
        return this.selfStage;
    }

    public RouterBase router(){
        return this.router;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public Scene getJavaFXScene() {
        return selfStage().getScene();
    }

    @Override
    public void whenReady(RunnableThrowing runnableThrowing) {
        Scene current = selfStage.getScene();
        if (current != null) {
            log.debug("Scene already available, executing callback immediately");
            try {
                runnableThrowing.run();
            } catch (Throwable t) {
                log.error("Async task failed", t);
                ErrorReporter.handle(t);
            }
            return;
        }

        log.debug("Scene not yet available, registering listener");
        selfStage.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                try {
                    runnableThrowing.run();
                } catch (Throwable t) {
                    log.error("Async task failed", t);
                    ErrorReporter.handle(t);
                }
            }
        });
    }

}