package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.FournisseurDAO;
import com.pharmacy.sgpa.model.Fournisseur;
import com.pharmacy.sgpa.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;

public class FournisseurController {

    // --- FXML UI COMPONENTS ---
    @FXML private TextField txtSearch; //

    @FXML private TextField txtNom;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtAdresse;

    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Integer> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colContact;
    @FXML private TableColumn<Fournisseur, String> colEmail;
    @FXML private TableColumn<Fournisseur, String> colAdresse;

    private FournisseurDAO dao;

    // MASTER DATA LIST (Holds everything)
    private ObservableList<Fournisseur> masterList = FXCollections.observableArrayList();

    // FILTERED VIEW (Holds only what matches search)
    private FilteredList<Fournisseur> filteredData;

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String PHONE_REGEX = "^\\+?[0-9 ]{10,15}$";

    @FXML
    public void initialize() {
        dao = new FournisseurDAO();

        // 1. Setup Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        // 2. Load Data into Master List
        masterList.addAll(dao.getAllFournisseurs());

        // 3. Wrap in FilteredList (Initially shows everything)
        filteredData = new FilteredList<>(masterList, p -> true);

        // 4. BIND SEARCH BAR (The Logic)
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(fournisseur -> {
                    // If filter text is empty, display all.
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();

                    // Search in Name
                    if (fournisseur.getNom().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    // Search in Contact/Phone
                    else if (fournisseur.getContact() != null && fournisseur.getContact().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    // Search in Email
                    else if (fournisseur.getEmail() != null && fournisseur.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    return false; // Does not match.
                });
            });
        }

        // 5. Wrap in SortedList (So sorting headers still works)
        SortedList<Fournisseur> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableFournisseurs.comparatorProperty());

        // 6. Set items to Table
        tableFournisseurs.setItems(sortedData);
    }

    // Helper to refresh data without breaking the search filter
    private void refreshData() {
        masterList.setAll(dao.getAllFournisseurs());
    }

    @FXML
    public void addFournisseur() {
        Window owner = txtNom.getScene().getWindow();

        String nom = txtNom.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String adresse = txtAdresse.getText().trim();

        // 1. Check Empty Fields
        if (nom.isEmpty() || contact.isEmpty() || email.isEmpty() || adresse.isEmpty()) {
            NavigationUtil.showNotification(owner, "Données manquantes", "Tous les champs sont obligatoires.");
            return;
        }

        // 2. Validate Email
        if (!email.matches(EMAIL_REGEX)) {
            NavigationUtil.showNotification(owner, "Format Incorrect", "L'adresse email n'est pas valide.");
            return;
        }

        // 3. Validate Phone
        if (!contact.matches(PHONE_REGEX)) {
            NavigationUtil.showNotification(owner, "Format Incorrect", "Le numéro de téléphone est invalide.");
            return;
        }

        // 4. Validate Address
        if (adresse.length() < 5) { // Relaxed constraint slightly
            NavigationUtil.showNotification(owner, "Adresse trop courte", "Veuillez entrer une adresse complète.");
            return;
        }

        // --- SAVE ---
        Fournisseur f = new Fournisseur(nom, contact, email, adresse);

        // Assuming your DAO returns void. If it returns boolean, check it.
        dao.addFournisseur(f);

        // Feedback & Refresh
        txtNom.clear();
        txtContact.clear();
        txtEmail.clear();
        txtAdresse.clear();

        refreshData(); // Updates the Master List

        NavigationUtil.showNotification(owner, "Succès", "Fournisseur ajouté avec succès !");
    }

    @FXML
    public void deleteFournisseur() {
        Window owner = tableFournisseurs.getScene().getWindow();
        Fournisseur selected = tableFournisseurs.getSelectionModel().getSelectedItem();

        if (selected == null) {
            NavigationUtil.showNotification(owner, "Erreur", "Veuillez sélectionner un fournisseur.");
            return;
        }

        dao.deleteFournisseur(selected.getId());
        refreshData(); // Updates the Master List
        NavigationUtil.showNotification(owner, "Suppression", "Fournisseur supprimé.");
    }

    @FXML
    public void backToMenu(javafx.event.ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/MainView.fxml");
    }
}