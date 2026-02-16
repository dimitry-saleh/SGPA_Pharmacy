package com.pharmacy.sgpa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set Window Icon
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.out.println("Icon not found.");
        }

        stage.setTitle("SGPA - Pharmacy Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}