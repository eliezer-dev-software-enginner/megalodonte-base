package megalodonte.application;

import javafx.application.Application;
import javafx.stage.Stage;

public final class JavaFXHost extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("JavaFXHost START CALLED");
        stage = primaryStage;
        
        primaryStage.setOnCloseRequest(e -> MegalodonteApp.triggerShutdown());
        
        Bootstrap.dispatch(primaryStage);
    }
}
