package org.example.ecommerce;

import util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set the main stage for the navigator so it can switch scenes
        SceneNavigator.setMainStage(primaryStage);
        // Load the initial scene (for example, Login screen)
        SceneNavigator.switchTo("/views/login.fxml");
        primaryStage.setTitle("E-Commerce App");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
