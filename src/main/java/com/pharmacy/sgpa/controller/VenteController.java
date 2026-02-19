package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.LoggerDAO;
import com.pharmacy.sgpa.dao.MedicamentDAO;
import com.pharmacy.sgpa.dao.VenteDAO;
import com.pharmacy.sgpa.model.LignePanier;
import com.pharmacy.sgpa.model.Medicament;
import com.pharmacy.sgpa.model.Vente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

import static com.pharmacy.sgpa.util.NavigationUtil.navigateTo;
import static com.pharmacy.sgpa.util.NavigationUtil.showNotification;

public class VenteController {

    // --- LEFT SIDE: Catalog ---
    @FXML private TextField txtSearch;
    @FXML private TextField txtQuantity;
    @FXML private TableView<Medicament> tableMedicaments;
    @FXML private TableColumn<Medicament, Integer> colMedId;
    @FXML private TableColumn<Medicament, String> colMedNom;
    @FXML private TableColumn<Medicament, Integer> colMedStock;
    @FXML private TableColumn<Medicament, Double> colMedPrix;

    // --- RIGHT SIDE: Basket (Panier) ---
    @FXML private TableView<LignePanier> tablePanier;
    @FXML private TableColumn<LignePanier, String> colPanierNom;
    @FXML private TableColumn<LignePanier, Integer> colPanierQte;
    @FXML private TableColumn<LignePanier, Double> colPanierTotal;
    @FXML private Label lblTotal;

    // Data
    private MedicamentDAO medicamentDAO;
    private ObservableList<Medicament> masterData = FXCollections.observableArrayList();
    private ObservableList<LignePanier> basketData = FXCollections.observableArrayList();

    //CheckBox
    @FXML private CheckBox chkSurOrdonnance;

    @FXML private HBox searchBarGroup;

    @FXML
    public void initialize() {
        medicamentDAO = new MedicamentDAO();

        // --- 1. SETUP CATALOG COLUMNS ---
        colMedId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMedNom.setCellValueFactory(new PropertyValueFactory<>("nomCommercial"));
        colMedStock.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));
        colMedPrix.setCellValueFactory(new PropertyValueFactory<>("prixPublic"));

        // Format Price (Catalog)
        colMedPrix.setCellFactory(c -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (!empty && price != null) {
                    setText(String.format("%.2f €", price));
                    setStyle("-fx-alignment: CENTER-RIGHT;"); // Align price to right
                } else {
                    setText(null);
                }
            }
        });

        // --- 2. SETUP BASKET COLUMNS ---
        colPanierNom.setCellValueFactory(new PropertyValueFactory<>("nomMedicament"));
        colPanierQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPanierTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        // Format Price (Basket)
        colPanierTotal.setCellFactory(c -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (!empty && price != null) {
                    setText(String.format("%.2f €", price));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;"); // Make total bold
                } else {
                    setText(null);
                }
            }
        });

        // --- 3. UI POLISH: SEARCH BAR GLOW ---
        // This makes the HBox container glow when the TextField is clicked
        if (searchBarGroup != null && txtSearch != null) {
            txtSearch.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    searchBarGroup.getStyleClass().add("search-bar-focused");
                } else {
                    searchBarGroup.getStyleClass().remove("search-bar-focused");
                }
            });
        }

        // --- 4. LOAD DATA ---
        loadCatalog();
        setupSearch();

        // --- 5. LINK BASKET ---
        tablePanier.setItems(basketData);
    }
    private void loadCatalog() {
        masterData.setAll(medicamentDAO.getAllMedicaments());
        tableMedicaments.setItems(masterData);
    }

    private void setupSearch() {
        // Wrap the list in a FilteredList
        FilteredList<Medicament> filteredData = new FilteredList<>(masterData, p -> true);

        // Listen to the text box
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(medicament -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();

                // Filter by Name OR Active Principle
                if (medicament.getNomCommercial().toLowerCase().contains(lowerCaseFilter)) return true;
                if (medicament.getPrincipeActif().toLowerCase().contains(lowerCaseFilter)) return true;

                return false;
            });
        });

        tableMedicaments.setItems(filteredData);
    }

    @FXML
    public void addToBasket() {
        javafx.stage.Window owner = tableMedicaments.getScene().getWindow();
        Medicament selected = tableMedicaments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(owner,"Attention", "Veuillez sélectionner un médicament.");
            return;
        }

        try {
            int qte = Integer.parseInt(txtQuantity.getText());
            if (qte <= 0) throw new NumberFormatException();

            if (qte > selected.getStockActuel()) {
                showNotification(owner,"Stock Insuffisant", "Il ne reste que " + selected.getStockActuel() + " boîtes.");
                return;
            }

            // Check if already in basket
            for (LignePanier lp : basketData) {
                if (lp.getMedicament().getId() == selected.getId()) {
                    showNotification(owner,"Déjà ajouté", "Ce produit est déjà dans le panier.");
                    return;
                }
            }

            // Add to Basket
            basketData.add(new LignePanier(selected, qte));
            updateTotal();

        } catch (NumberFormatException e) {
            showNotification(owner,"Erreur", "Quantité invalide.");
        }
    }

    @FXML
    public void clearBasket() {
        basketData.clear();
        updateTotal();
    }

    private void updateTotal() {
        double total = 0.0;
        for (LignePanier lp : basketData) {
            total += lp.getSousTotal();
        }
        lblTotal.setText(String.format("%.2f €", total));
    }



    @FXML
    public void showStock(javafx.event.ActionEvent event) {
        navigateTo(event,"/fxml/MainView.fxml");
    }

    @FXML
    public void processSale() {
        javafx.stage.Window owner = tablePanier.getScene().getWindow();
        if (basketData.isEmpty()) {
            showNotification(owner,"Panier Vide", "Ajoutez des médicaments avant de valider.");
            return;
        }

        // 1. Prepare Data
        Vente vente = new Vente();
        vente.setSurOrdonnance(chkSurOrdonnance.isSelected());
        vente.setMontantTotal(basketData.stream().mapToDouble(LignePanier::getSousTotal).sum());
        vente.setLignes(new ArrayList<>(basketData));

        // 2. Call DAO
        VenteDAO venteDAO = new VenteDAO();
        boolean success = venteDAO.saveVente(vente);

        if (success) {
            // 3. Success Feedback
            showNotification(owner,"Vente Validée", "La vente a été enregistrée avec succès !");


            // 4. Clear UI
            clearBasket();
            loadCatalog(); // IMPORTANT: Reload catalog to see updated stock!

            LoggerDAO.log("Nouvelle Vente validée. Montant: " + vente.getMontantTotal() + "€");

        } else {
            showNotification(owner,"Erreur", "La vente n'a pas pu être enregistrée.");
        }
    }
}