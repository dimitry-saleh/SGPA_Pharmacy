package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.MedicamentDAO;
import com.pharmacy.sgpa.model.Medicament;
import com.pharmacy.sgpa.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // This import is now active
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TableRow;
import java.time.LocalDate;
import java.util.Optional;
import java.io.IOException;
import com.pharmacy.sgpa.model.Fournisseur;
import com.pharmacy.sgpa.dao.FournisseurDAO;
import javafx.util.StringConverter;

import static com.pharmacy.sgpa.util.NavigationUtil.navigateTo;
import static com.pharmacy.sgpa.util.NavigationUtil.showNotification;

public class MainController {

    @FXML private TableView<Medicament> medicamentTable;
    @FXML private TableColumn<Medicament, Integer> colId;
    @FXML private TableColumn<Medicament, String> colNom;
    @FXML private TableColumn<Medicament, String> colPrincipe;
    @FXML private TableColumn<Medicament, Integer> colStock;
    @FXML private TableColumn<Medicament, Double> colPrix;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private HBox searchBarGroup;
    @FXML private TextField txtSearch;


    private MedicamentDAO medicamentDAO;
    private ObservableList<Medicament> masterData = FXCollections.observableArrayList();
    // filteredData is a "view" of masterData that changes as you type
    private FilteredList<Medicament> filteredData;


    @FXML
    public void initialize() {
        medicamentDAO = new MedicamentDAO();
        setupTableColumns();

        // 1. INITIALIZE FILTERING ARCHITECTURE
        filteredData = new FilteredList<>(masterData, p -> true);
        medicamentTable.setItems(filteredData);

        // 2. SEARCH BAR GLOW
        txtSearch.focusedProperty().addListener((obs, old, newVal) -> {
            if (newVal) searchBarGroup.getStyleClass().add("search-bar-focused");
            else searchBarGroup.getStyleClass().remove("search-bar-focused");
        });

        // 3. REAL-TIME SEARCH LOGIC
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(medicament -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return medicament.getNomCommercial().toLowerCase().contains(lowerCaseFilter) ||
                        medicament.getPrincipeActif().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // 4. REWRITTEN ROW ALERT STYLING (Critical Red & Warning Yellow)
        medicamentTable.setRowFactory(tv -> new TableRow<Medicament>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);

                // Clear all previous custom styles to prevent "ghost" colors on empty rows
                getStyleClass().removeAll("row-stock-critique", "row-stock-warning", "row-alert-expiry");

                if (item != null && !empty) {
                    int currentStock = item.getStockActuel();
                    int threshold = item.getSeuilMin();

                    // Logic: Red if at or below threshold, Yellow if within 20% of reaching it
                    if (currentStock <= threshold) {
                        getStyleClass().add("row-stock-critique"); // RED
                    } else if (currentStock <= threshold * 1.25) {
                        getStyleClass().add("row-stock-warning");  // YELLOW
                    }

                    // Keep expiry styling as a secondary check if not already colored by stock
                    boolean perimeBientot = item.getDatePeremption() != null &&
                            item.getDatePeremption().isBefore(LocalDate.now().plusMonths(3));

                    if (perimeBientot && !getStyleClass().contains("row-stock-critique")) {
                        getStyleClass().add("row-alert-expiry");
                    }
                }
            }
        });

        // 5. BUTTON BINDINGS
        btnModifier.disableProperty().bind(medicamentTable.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimer.disableProperty().bind(medicamentTable.getSelectionModel().selectedItemProperty().isNull());

        loadData();
    }
    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            medicamentTable.setItems(masterData); // 'masterData' should be your full list from the DAO
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();

        // Filter the list based on Name or Principle
        ObservableList<Medicament> filteredList = masterData.filtered(medicament -> {
            return medicament.getNomCommercial().toLowerCase().contains(lowerCaseFilter) ||
                    medicament.getPrincipeActif().toLowerCase().contains(lowerCaseFilter);
        });

        medicamentTable.setItems(filteredList);
    }

    private void setupTableColumns() {
        // Map the Table Columns to the Medicament class properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCommercial"));
        colPrincipe.setCellValueFactory(new PropertyValueFactory<>("principeActif"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixPublic"));

        colPrix.setCellFactory(column -> new javafx.scene.control.TableCell<Medicament, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", price));
                }
            }
        });

    }



    @FXML
    void loadData() {
        txtSearch.clear(); // Optional: Resets the search filter when data is reloaded
        // 1. Fetch fresh data from Database
        // We use .setAll() on masterData.
        // This updates the list WITHOUT breaking the link to the FilteredList or the Table.
        masterData.setAll(medicamentDAO.getAllMedicaments());

        // 2. The TableView and FilteredList update AUTOMATICALLY
        // because they are observing masterData. We don't need .setItems() anymore.

        // 3. Clear selection so the user doesn't accidentally edit the wrong row
        medicamentTable.getSelectionModel().clearSelection();

        // Note: medicamentTable.refresh() is usually not needed when using ObservableLists properly,
        // but you can keep it if you notice any visual artifacts.
    }
    @FXML
    public void showVentes(ActionEvent event) {
        navigateTo(event,"/fxml/VenteView.fxml");
    }
    @FXML
    public void handleAddMedicament() {
        try {
            // 1. Load the FXML for the form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormView.fxml"));
            Parent root = loader.load();

            // 2. Get the controller of the new form to pass the "this" reference
            FormController formController = loader.getController();
            formController.setMainController(this); // Pass "this" so it can refresh our table later

            // 3. Create the window (Stage)
            Stage stage = new Stage();
            stage.setTitle("Ajout Médicament");
            stage.setScene(new Scene(root));

            // 4. Block interaction with main window until this popup is closed
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FormView.fxml: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditMedicament() {
        javafx.stage.Window owner = medicamentTable.getScene().getWindow();
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(owner,"Aucune sélection", "Veuillez sélectionner un médicament à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormView.fxml"));
            Parent root = loader.load();

            FormController formController = loader.getController();
            formController.setMainController(this);
            formController.preloadData(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier Médicament");
            stage.setScene(new Scene(root));

            // 1. Set the OWNER. This is crucial for Full Screen.
            // It tells JavaFX that the popup belongs to the main window.
            stage.initOwner(medicamentTable.getScene().getWindow());

            stage.initModality(Modality.APPLICATION_MODAL);

            // 2. Use showAndWait() instead of show()
            // This pauses the MainController logic here until the window is CLOSED.
            stage.showAndWait();

            // 3. Force a UI refresh after the window is definitely gone
            loadData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleDeleteMedicament() {
        javafx.stage.Window owner = medicamentTable.getScene().getWindow();
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(owner,"Aucune sélection", "Veuillez sélectionner un médicament à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Voulez-vous vraiment supprimer " + selected.getNomCommercial() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            medicamentDAO.deleteMedicament(selected.getId());
            loadData(); // Refresh table
        }
    }

    @FXML
    public void showHistorique(javafx.event.ActionEvent event) {
        navigateTo(event,"/fxml/HistoriqueView.fxml");
    }
    @FXML
    public void showFournisseurs(javafx.event.ActionEvent event) {
        navigateTo(event,"/fxml/FournisseurView.fxml");
    }

    @FXML
    public void showCommandes(javafx.event.ActionEvent event) {
        navigateTo(event,"/fxml/CommandeView.fxml");
    }

    @FXML
    public void showDashboard(javafx.event.ActionEvent event) {
        navigateTo(event,"/fxml/DashboardView.fxml");
    }



    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {



        UserSession.cleanUserSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            // Get current stage from the button that was clicked
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}