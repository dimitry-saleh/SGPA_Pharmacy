package com.pharmacy.sgpa.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.event.MouseEvent;
import java.io.IOException;

public class NavigationUtil {

    public static void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            // 1. Load the new FXML
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent newRoot = loader.load();

            // 2. Get the current Stage and Scene
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();

            // 3. THE MAGIC: Replace the content, NOT the scene
            // This keeps the window size, position, and maximized state exactly as is.
            scene.setRoot(newRoot);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to: " + fxmlPath);
        }
    }


    // --- HELPER METHOD (To avoid copy-pasting code) ---
    // Move the actual loading logic here so both methods use it
    private static void performNavigation(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        // If you want to keep full screen, you might need stage.setFullScreen(true) here
        // depending on your preference.
    }

    public static void logout(ActionEvent event) {
        try {
            Stage stage = getStageFromEvent(event);
            if (stage == null) return;

            UserSession.cleanUserSession();

            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            // Reset scene for login (resize window)
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Stage getStageFromEvent(ActionEvent event) {
        Object source = event.getSource();

        // Case 1: The event comes from a Button (Node)
        if (source instanceof Node) {
            return (Stage) ((Node) source).getScene().getWindow();
        }

        // Case 2: The event comes from a Dropdown Item (MenuItem)
        else if (source instanceof MenuItem) {
            MenuItem item = (MenuItem) source;
            // We have to get the "Parent Popup" (the dropdown menu),
            // then get the "Owner Node" (the button that opened it)
            if (item.getParentPopup() != null) {
                Node owner = item.getParentPopup().getOwnerNode();
                if (owner != null) {
                    return (Stage) owner.getScene().getWindow();
                }
            }
        }

        System.err.println("Could not determine Stage from event source: " + source);
        return null;
    }


    public static void showNotification(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // CRITICAL: Link the alert to the main full-screen window
        if (owner != null) {
            alert.initOwner(owner);
        }

        // Ensure it doesn't create a taskbar icon and stays on top
        alert.initModality(Modality.APPLICATION_MODAL);

        alert.showAndWait();
    }

    public static void navigateTo(javafx.scene.input.MouseEvent event, String fxmlPath) {

        try {
            // 1. Load the new FXML
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent newRoot = loader.load();

            // 2. Get the current Stage and Scene
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();

            // 3. THE MAGIC: Replace the content, NOT the scene
            // This keeps the window size, position, and maximized state exactly as is.
            scene.setRoot(newRoot);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to: " + fxmlPath);
        }
    }
}