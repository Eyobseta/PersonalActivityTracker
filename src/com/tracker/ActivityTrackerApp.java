package com.tracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class ActivityTrackerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/resources/main.fxml");
        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/resources/style.css").toExternalForm());
        primaryStage.setTitle("Personal Activity Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}