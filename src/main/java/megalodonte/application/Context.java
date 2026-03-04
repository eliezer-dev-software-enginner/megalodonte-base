package megalodonte.application;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import megalodonte.base.ComponentInterface;
import megalodonte.base.RouteProps;
import megalodonte.base.RouterBase;

public final class Context {

    private final Stage stage;
    private boolean autoShow = true;
    public final double width = 600, height = 500;

    Context(Stage stage) {
        this.stage = stage;
    }

    public Stage javafxStage() {
        return stage;
    }

    public void disableAutoShow() {
        this.autoShow = false;
    }

    boolean shouldAutoShow() {
        return autoShow;
    }

    public void useView(ComponentInterface<?> component) {
        stage.setScene(new Scene((Parent) component.getJavaFxNode(), width, height));
    }

    public void useView(View view) {
        stage.setScene(new Scene((Parent) view.render().getJavaFxNode(), width, height));
    }

/**
     * Usado internamente pelo Router
     * @param view
     * @param props
     */
//    public void useView(View view, RouteProps props) {
//        if (stage.getScene() == null) {
//            stage.setScene(new Scene((Parent) view.render().getJavaFxNode(), props.screenWidth(), props.screenHeight()));
//        } else {
//            stage.getScene().setRoot((Parent) view.render().getJavaFxNode());
//            // Atualiza as dimensões do Stage existente
//            stage.setWidth(props.screenWidth());
//            stage.setHeight(props.screenHeight());
//        }
//
//        stage.centerOnScreen();
//        stage.setResizable(props.screenIsExpandable());
//        stage.setTitle(props.name());
//    }

    public void useView(View view, RouteProps props) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene((Parent) view.render().getJavaFxNode(), props.screenWidth(), props.screenHeight()));
        } else {
            stage.getScene().setRoot((Parent) view.render().getJavaFxNode());
            stage.setWidth(props.screenWidth());
            stage.setHeight(props.screenHeight());
        }

        stage.setResizable(props.screenIsExpandable());
        stage.setTitle(props.name());

        // Agendamento para garantir que o layout seja processado
        Platform.runLater(() -> {
            stage.centerOnScreen();
            stage.sizeToScene();
        });
}

    public RouterBase useRouter(RouterBase router) {
        router.bind(this);
        return router;
    }
}
