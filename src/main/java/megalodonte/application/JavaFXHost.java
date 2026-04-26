package megalodonte.application;

import javafx.application.Application;
import javafx.stage.Stage;

public final class JavaFXHost extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("JavaFXHost START CALLED");

        primaryStage.setOnCloseRequest(e -> Bootstrap.dispatchEvent(MegalodonteApp.Event.CloseRequest));
        
        Bootstrap.dispatch(primaryStage);
    }
}
