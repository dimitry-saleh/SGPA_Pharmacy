package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.util.DatabaseConnection;
import com.pharmacy.sgpa.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.pharmacy.sgpa.util.NavigationUtil.showNotification;

public class RegisterController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private ComboBox<String> comboRole;

    @FXML
    public void initialize() {
        comboRole.getItems().addAll("PHARMACIEN", "ADMIN");
        comboRole.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleRegister() {
        String user = txtUser.getText().trim();
        String pass = txtPass.getText().trim();
        String role = comboRole.getValue();

        // Use an FXML node for the owner - 100% crash-proof
        javafx.stage.Window owner = txtUser.getScene().getWindow();

        if (user.isEmpty() || pass.isEmpty()) {
            showNotification(owner, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // --- THE KEY PART: HASHING ---
        String hashedPassword = PasswordUtil.hashPassword(pass);

        String sql = "INSERT INTO utilisateur (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.executeUpdate();

            showNotification(owner, "Succès", "Utilisateur créé !");

            // Close the popup - Dashboard will automatically be visible underneath
            ((Stage) owner).close();

        } catch (Exception e) {
            e.printStackTrace();
            showNotification(owner, "Erreur", "Ce nom d'utilisateur existe déjà ou une erreur SQL est survenue.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        // Safer way: get the window from the FXML component instead of casting the event source
        Stage stage = (Stage) txtUser.getScene().getWindow();
        stage.close();
    }
}