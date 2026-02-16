package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.LoggerDAO;
import com.pharmacy.sgpa.util.DatabaseConnection;
import com.pharmacy.sgpa.util.PasswordUtil;
import com.pharmacy.sgpa.util.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblError;

    // Links to the HBox containers in FXML for the "Glow" effect
    @FXML private HBox userInputGroup;
    @FXML private HBox passInputGroup;

    @FXML
    public void initialize() {
        // Apply focus effects to containers (Fixes the 'focused-within' issue)
        setupFocusEffect(txtUser, userInputGroup);
        setupFocusEffect(txtPass, passInputGroup);

        // Clear error message when user starts typing again
        txtUser.textProperty().addListener((obs) -> lblError.setText(""));
        txtPass.textProperty().addListener((obs) -> lblError.setText(""));
    }

    private void setupFocusEffect(Control input, HBox group) {
        input.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                group.getStyleClass().add("input-group-focused");
            } else {
                group.getStyleClass().remove("input-group-focused");
            }
        });
    }

    @FXML
    public void handleLogin() {
        String user = txtUser.getText().trim();
        String pass = txtPass.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        String sql = "SELECT * FROM utilisateur WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (PasswordUtil.checkPassword(pass, storedHash)) {
                    UserSession.setSession(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                    LoggerDAO.log("Connexion au système");
                    openDashboard();
                } else {
                    showError("Mot de passe incorrect.");
                }
            } else {
                showError("Utilisateur inconnu.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données.");
        }
    }

    /**
     * Professional error feedback: Sets text AND triggers a shake animation
     */
    private void showError(String message) {
        lblError.setText(message);
        shakeNode(userInputGroup.getParent()); // Shakes the whole card
    }

    private void shakeNode(javafx.scene.Node node) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0), new javafx.animation.KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new javafx.animation.KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(100), new javafx.animation.KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(150), new javafx.animation.KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new javafx.animation.KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(250), new javafx.animation.KeyValue(node.translateXProperty(), 0))
        );
        timeline.play();
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent root = loader.load();

            // 1. Get the current Stage from an existing UI element
            Stage stage = (Stage) txtUser.getScene().getWindow();

            // 2. THE FIX: Check if we already have a scene
            if (stage.getScene() != null) {
                // Swap only the content. Window size and maximized state are preserved.
                stage.getScene().setRoot(root);
            } else {
                // Fallback for the very first window shown
                stage.setScene(new Scene(root));
            }

            // 3. Optional: Only center if the window wasn't already visible/resized
            // stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}