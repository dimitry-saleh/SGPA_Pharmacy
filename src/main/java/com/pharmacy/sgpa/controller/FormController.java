package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.FournisseurDAO;
import com.pharmacy.sgpa.dao.MedicamentDAO;
import com.pharmacy.sgpa.model.Fournisseur;
import com.pharmacy.sgpa.model.Medicament;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import static com.pharmacy.sgpa.util.NavigationUtil.showNotification;

public class FormController {

    // --- FXML FIELDS ---
    @FXML private TextField txtNom;
    @FXML private TextField txtPrincipe;
    @FXML private TextField txtForme;
    @FXML private TextField txtDosage;
    @FXML private TextField txtPrixAchat;
    @FXML private TextField txtPrixPublic;
    @FXML private TextField txtStock;
    @FXML private TextField txtSeuil;
    @FXML private DatePicker datePeremption;
    @FXML private CheckBox chkOrdonnance;
    @FXML private Label lblTitle;

    // The Critical Dropdown for Foreign Key
    @FXML private ComboBox<Fournisseur> comboFournisseur;

    // --- CONTROLLER VARIABLES ---
    private MedicamentDAO dao;
    private FournisseurDAO fournDAO;
    private MainController mainController;
    private Medicament currentMedicament;

    @FXML
    public void initialize() {
        this.dao = new MedicamentDAO();
        this.fournDAO = new FournisseurDAO();

        // Load the suppliers immediately so the dropdown is never empty
        loadSuppliers();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    /**
     * Loads suppliers into the ComboBox and configures how they are displayed.
     */
    private void loadSuppliers() {
        // 1. Get List from DB
        comboFournisseur.setItems(FXCollections.observableArrayList(fournDAO.getAll()));

        // 2. Configure Display (Show Name, Store Object)
        comboFournisseur.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur f) {
                return (f != null) ? f.getNom() : "";
            }

            @Override
            public Fournisseur fromString(String string) {
                return comboFournisseur.getItems().stream()
                        .filter(f -> f.getNom().equals(string))
                        .findFirst().orElse(null);
            }
        });
    }

    /**
     * Called when editing an existing medicament.
     */
    public void preloadData(Medicament m) {
        this.currentMedicament = m;

        // 1. Set Header
        if (lblTitle != null) lblTitle.setText("Modifier le Médicament");

        // 2. Fill Text Fields
        txtNom.setText(m.getNomCommercial());
        txtPrincipe.setText(m.getPrincipeActif());
        txtForme.setText(m.getFormeGalenique());
        txtDosage.setText(m.getDosage());

        // Handle numbers safely (convert to String)
        txtPrixAchat.setText(String.valueOf(m.getPrixAchat()));
        txtPrixPublic.setText(String.valueOf(m.getPrixPublic()));
        txtStock.setText(String.valueOf(m.getStockActuel()));
        txtSeuil.setText(String.valueOf(m.getSeuilMin()));

        // Handle Date and Checkbox
        datePeremption.setValue(m.getDatePeremption());
        chkOrdonnance.setSelected(m.isNecessiteOrdonnance());

        // --- 3. THE FIX: SELECT THE EXISTING SUPPLIER ---
        // We loop through the ComboBox items to find the matching ID
        int targetId = m.getFournisseurId();

        if (targetId > 0 && comboFournisseur.getItems() != null) {
            for (Fournisseur f : comboFournisseur.getItems()) {
                if (f.getId() == targetId) {
                    // We found the supplier object! Select it.
                    comboFournisseur.getSelectionModel().select(f);
                    return; // Stop searching once found
                }
            }
        }
    }

    @FXML
    public void saveMedicament() {
        javafx.stage.Window owner = txtNom.getScene().getWindow();
        // 1. BASIC VALIDATION
        if (txtNom.getText().isEmpty() || txtPrixPublic.getText().isEmpty()) {
            showNotification(owner,"Erreur de Validation", "Le Nom et le Prix sont obligatoires.");
            return;
        }

        // 2. CRITICAL VALIDATION: PREVENT SQL FOREIGN KEY ERROR
        if (comboFournisseur.getValue() == null) {
            showNotification(owner,"Erreur Fournisseur", "Veuillez sélectionner un fournisseur dans la liste.");
            return;
        }

        try {
            if (currentMedicament == null) {
                currentMedicament = new Medicament();
            }

            // 3. SET DATA (Using safe parsing helper)
            currentMedicament.setNomCommercial(txtNom.getText());
            currentMedicament.setPrincipeActif(txtPrincipe.getText());
            currentMedicament.setFormeGalenique(txtForme.getText());
            currentMedicament.setDosage(txtDosage.getText());

            currentMedicament.setPrixAchat(parseDouble(txtPrixAchat.getText()));
            currentMedicament.setPrixPublic(parseDouble(txtPrixPublic.getText()));
            currentMedicament.setStockActuel((int) parseDouble(txtStock.getText()));
            currentMedicament.setSeuilMin((int) parseDouble(txtSeuil.getText()));

            currentMedicament.setDatePeremption(datePeremption.getValue());
            currentMedicament.setNecessiteOrdonnance(chkOrdonnance.isSelected());

            // 4. SET THE FOREIGN KEY ID
            // We already checked it's not null in step 2
            currentMedicament.setFournisseurId(comboFournisseur.getValue().getId());

            // 5. SAVE TO DB
            if (currentMedicament.getId() > 0) {
                dao.updateMedicament(currentMedicament);
            } else {
                dao.addMedicament(currentMedicament);
            }

            // 6. REFRESH MAIN VIEW
            if (mainController != null) {
                mainController.loadData();
            }
            closeWindow();

        } catch (NumberFormatException e) {
            showNotification(owner, "Erreur de Format", "Veuillez vérifier les prix et quantités (chiffres uniquement).");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification(owner,"Erreur Système", "Impossible de sauvegarder : " + e.getMessage());
        }
    }

    /**
     * Safe helper to parse numbers like "12,50" or "12.50"
     */
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        return Double.parseDouble(value.replace(",", "."));
    }



    @FXML
    public void closeWindow() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}