package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.CommandeDAO;
import com.pharmacy.sgpa.dao.FournisseurDAO;
import com.pharmacy.sgpa.dao.MedicamentDAO;
import com.pharmacy.sgpa.model.Commande;
import com.pharmacy.sgpa.model.Fournisseur;
import com.pharmacy.sgpa.model.LignePanier;
import com.pharmacy.sgpa.model.Medicament;
import com.pharmacy.sgpa.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandeController {

    // --- TAB 1: NEW ORDER ---
    @FXML private ComboBox<Fournisseur> comboFournisseur;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtSearch; // Search Bar
    @FXML private CheckBox chkShowAll; // Toggle Button/Checkbox

    // Source Table (Stock)
    @FXML private TableView<Medicament> tableAlerts;
    @FXML private TableColumn<Medicament, String> colNom;
    @FXML private TableColumn<Medicament, Integer> colStock;
    @FXML private TableColumn<Medicament, Integer> colSeuil;

    // Basket Table (Current Order)
    @FXML private TableView<LignePanier> tableOrder;
    @FXML private TableColumn<LignePanier, String> colOrderNom;
    @FXML private TableColumn<LignePanier, Integer> colOrderQte;

    // --- TAB 2: RECEPTION ---
    // Pending Orders List
    @FXML private TableView<Commande> tablePending;
    @FXML private TableColumn<Commande, Integer> colCmdId;
    @FXML private TableColumn<Commande, String> colCmdDate;
    @FXML private TableColumn<Commande, String> colCmdFournisseur;
    @FXML private TableColumn<Commande, String> colCmdStatut;

    // Order Details (What's inside the selected order)
    @FXML private TableView<LignePanier> tableReceptionDetails;
    @FXML private TableColumn<LignePanier, String> colDetailNom;
    @FXML private TableColumn<LignePanier, Integer> colDetailQte;

    // --- DATA ---
    private ObservableList<LignePanier> orderList = FXCollections.observableArrayList();
    private ObservableList<Medicament> masterData = FXCollections.observableArrayList();
    private FilteredList<Medicament> filteredData;
    private Map<Integer, String> supplierMap = new HashMap<>();

    // DAOs
    private MedicamentDAO medDAO;
    private FournisseurDAO fournDAO;
    private CommandeDAO cmdDAO;

    @FXML
    public void initialize() {
        medDAO = new MedicamentDAO();
        fournDAO = new FournisseurDAO();
        cmdDAO = new CommandeDAO();

        setupTableColumns();

        // 1. SETUP SUPPLIER COMBO (With "All" Dummy Option)
        List<Fournisseur> suppliers = fournDAO.getAllFournisseurs();

        // Create Dummy "All" Option
        Fournisseur allSuppliers = new Fournisseur();
        allSuppliers.setId(-1);
        allSuppliers.setNom("Tous les fournisseurs");
        suppliers.add(0, allSuppliers); // Add to top

        comboFournisseur.setItems(FXCollections.observableArrayList(suppliers));
        comboFournisseur.getSelectionModel().selectFirst(); // Select "All" by default

        // Converter to show names in combo
        comboFournisseur.setConverter(new StringConverter<Fournisseur>() {
            @Override public String toString(Fournisseur f) { return f == null ? "" : f.getNom(); }
            @Override public Fournisseur fromString(String string) { return null; }
        });

        // Fill Lookup Map for Search (Skip dummy)
        for (Fournisseur f : suppliers) {
            if (f.getId() != -1) supplierMap.put(f.getId(), f.getNom());
        }

        // 2. LOAD MASTER DATA
        masterData.addAll(medDAO.getAllMedicaments());
        filteredData = new FilteredList<>(masterData, p -> true);

        // 3. BIND LISTENERS (Reactive Filtering)
        if (txtSearch != null) txtSearch.textProperty().addListener((obs, old, newVal) -> updateFilter());
        comboFournisseur.valueProperty().addListener((obs, old, newVal) -> updateFilter());
        if (chkShowAll != null) chkShowAll.selectedProperty().addListener((obs, old, newVal) -> updateFilter());

        // 4. BIND TABLES
        // Stock Table (Filtered & Sorted)
        SortedList<Medicament> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableAlerts.comparatorProperty());
        tableAlerts.setItems(sortedData);

        // Basket Table
        colOrderNom.setCellValueFactory(new PropertyValueFactory<>("nomMedicament"));
        colOrderQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        tableOrder.setItems(orderList);

        // Reception Tables
        setupReceptionTab();

        // Apply initial filter
        updateFilter();
    }

    private void setupTableColumns() {
        // Stock Columns
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCommercial"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActuel"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilMin"));
    }

    // --- FILTER LOGIC ---
    private void updateFilter() {
        filteredData.setPredicate(medicament -> {
            // 1. STOCK STATUS (Danger vs All)
            // If checkbox is UNCHECKED, we hide healthy stock
            if (chkShowAll != null && !chkShowAll.isSelected()) {
                if (medicament.getStockActuel() > medicament.getSeuilMin()) {
                    return false;
                }
            }

            // 2. SUPPLIER FILTER
            Fournisseur selectedSupplier = comboFournisseur.getValue();
            if (selectedSupplier != null && selectedSupplier.getId() != -1) {
                if (medicament.getFournisseurId() != selectedSupplier.getId()) {
                    return false;
                }
            }

            // 3. SEARCH BAR
            if (txtSearch != null) {
                String text = txtSearch.getText();
                if (text != null && !text.isEmpty()) {
                    String lower = text.toLowerCase();
                    boolean matchName = medicament.getNomCommercial().toLowerCase().contains(lower);

                    String sName = supplierMap.get(medicament.getFournisseurId());
                    boolean matchSupp = (sName != null && sName.toLowerCase().contains(lower));

                    if (!matchName && !matchSupp) return false;
                }
            }
            return true;
        });
    }

    // --- ORDER ACTIONS ---
    @FXML
    public void addToOrder() {
        Window owner = tableAlerts.getScene().getWindow();
        Medicament selected = tableAlerts.getSelectionModel().getSelectedItem();

        if (selected == null) {
            NavigationUtil.showNotification(owner, "Erreur", "Veuillez sélectionner un produit.");
            return;
        }

        // --- RESTRICTION: NO MIXED SUPPLIERS ---
        if (!orderList.isEmpty()) {
            Medicament firstItem = orderList.get(0).getMedicament();
            if (firstItem.getFournisseurId() != selected.getFournisseurId()) {
                NavigationUtil.showNotification(owner, "Action Interdite",
                        "Impossible de mélanger les fournisseurs.\nVeuillez terminer la commande actuelle d'abord.");
                return;
            }
        }

        try {
            String qtyText = txtQuantity.getText();
            if (qtyText == null || qtyText.isEmpty()) throw new NumberFormatException();
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) throw new NumberFormatException();

            orderList.add(new LignePanier(selected, qty));
            //txtQuantity.clear();
        } catch (NumberFormatException e) {
            NavigationUtil.showNotification(owner, "Erreur", "Quantité invalide.");
        }
    }

    @FXML
    public void saveOrder() {
        Window owner = comboFournisseur.getScene().getWindow();
        Fournisseur f = comboFournisseur.getValue();

        // Check if supplier is valid (not null and not dummy)
        if (f == null || f.getId() == -1) {
            // Attempt to infer supplier from the first item in the basket
            if (!orderList.isEmpty()) {
                int id = orderList.get(0).getMedicament().getFournisseurId();
                // We need the object to save the command.
                // In a real app, you might fetch it from DAO,
                // but here we can iterate the combo items to find the matching object.
                for (Fournisseur item : comboFournisseur.getItems()) {
                    if (item.getId() == id) {
                        f = item;
                        break;
                    }
                }
            }
        }

        if (f == null || f.getId() == -1 || orderList.isEmpty()) {
            NavigationUtil.showNotification(owner, "Validation", "Panier vide ou fournisseur invalide.");
            return;
        }

        Commande cmd = new Commande();
        cmd.setFournisseur(f);
        cmd.setLignes(new java.util.ArrayList<>(orderList));

        if (cmdDAO.createCommande(cmd)) {
            NavigationUtil.showNotification(owner, "Succès", "Commande envoyée au fournisseur !");
            orderList.clear();
            refreshPendingList();
        } else {
            NavigationUtil.showNotification(owner, "Erreur", "Erreur lors de l'enregistrement.");
        }
    }

    // --- RECEPTION TAB ---
    // Dans CommandeController.java

    private void setupReceptionTab() {
        // --- Table de gauche (Liste des commandes) ---
        colCmdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCmdDate.setCellValueFactory(new PropertyValueFactory<>("dateFormatee"));
        colCmdFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));
        colCmdStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // --- Table de droite (Détails) : C'EST ICI QUE CA BLOQUAIT ---

        // 1. Pour le NOM : On va chercher dans l'objet Medicament imbriqué
        colDetailNom.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMedicament().getNomCommercial()
                )
        );

        // 2. Pour la QUANTITÉ : C'est direct
        colDetailQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));


        // --- Chargement initial et Listener ---
        refreshPendingList();

        // Quand on clique sur une commande à gauche, on charge les détails à droite
        tablePending.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<LignePanier> details = cmdDAO.getLignesByCommandeId(newVal.getId());
                tableReceptionDetails.setItems(FXCollections.observableArrayList(details));
            } else {
                tableReceptionDetails.getItems().clear();
            }
        });
    }

    private void refreshPendingList() {
        tablePending.setItems(FXCollections.observableArrayList(cmdDAO.getPendingCommandes()));
    }

    @FXML
    public void handleReception() {
        Window owner = tablePending.getScene().getWindow();
        Commande selected = tablePending.getSelectionModel().getSelectedItem();

        if (selected == null) {
            NavigationUtil.showNotification(owner, "Erreur", "Sélectionnez une commande à recevoir.");
            return;
        }

        try {
            cmdDAO.receptionnerCommande(selected.getId());
            NavigationUtil.showNotification(owner, "Succès", "Réception validée. Stock mis à jour !");

            refreshPendingList();
            tableReceptionDetails.getItems().clear();

            // Refresh Stock Table
            masterData.setAll(medDAO.getAllMedicaments());
            updateFilter();

        } catch (Exception e) {
            e.printStackTrace();
            NavigationUtil.showNotification(owner, "Erreur", "Échec de la réception.");
        }
    }

    @FXML
    public void backToMenu(javafx.event.ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/MainView.fxml");
    }
}