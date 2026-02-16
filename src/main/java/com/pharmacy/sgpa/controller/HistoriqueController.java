package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.VenteDAO;
import com.pharmacy.sgpa.model.LignePanier;
import com.pharmacy.sgpa.model.Vente;
import com.pharmacy.sgpa.util.NavigationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class HistoriqueController {

    // --- FXML UI Components ---
    @FXML private TableView<Vente> tableVentes;
    @FXML private TableColumn<Vente, Integer> colId;
    @FXML private TableColumn<Vente, String> colDate;
    @FXML private TableColumn<Vente, Double> colTotal;
    @FXML private TableColumn<Vente, Boolean> colOrdonnance;

    @FXML private TableView<LignePanier> tableDetails;
    @FXML private TableColumn<LignePanier, String> colDetailNom;
    @FXML private TableColumn<LignePanier, Integer> colDetailQte;
    @FXML private TableColumn<LignePanier, Double> colDetailPrix;
    @FXML private TableColumn<LignePanier, Double> colDetailSousTotal;

    @FXML private Label lblSelectedId; // The dynamic header for the detail section

    private VenteDAO venteDAO;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm");

    @FXML
    public void initialize() {
        venteDAO = new VenteDAO();

        // ---------------------------------------------------------
        // 1. SETUP MASTER TABLE (VENTES)
        // ---------------------------------------------------------
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Date Column with custom formatting
        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateVente().format(dtf)));

        // Total Amount with Currency Formatting
        colTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colTotal.setCellFactory(column -> createCurrencyCell());

        // Prescription Status with Visual "Pills"
        colOrdonnance.setCellValueFactory(new PropertyValueFactory<>("surOrdonnance"));
        colOrdonnance.setCellFactory(column -> new TableCell<Vente, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label pill = new Label(item ? "ORDONNANCE" : "VENTE LIBRE");
                    pill.getStyleClass().add(item ? "pill-red" : "pill-green");
                    setGraphic(pill);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // ---------------------------------------------------------
        // 2. SETUP DETAIL TABLE (LIGNES PANIER)
        // ---------------------------------------------------------
        colDetailNom.setCellValueFactory(new PropertyValueFactory<>("nomMedicament"));
        colDetailQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        // Unit Price (Mapping from the Medicament object inside LignePanier)
        colDetailPrix.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getMedicament().getPrixPublic()));
        colDetailPrix.setCellFactory(column -> createCurrencyCell());

        // Subtotal
        colDetailSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        colDetailSousTotal.setCellFactory(column -> createCurrencyCell());

        // ---------------------------------------------------------
        // 3. EVENT LISTENERS & INITIAL LOAD
        // ---------------------------------------------------------

        // Load all sales initially
        tableVentes.setItems(FXCollections.observableArrayList(venteDAO.getAllVentes()));

        // Listener to load details when a sale is selected
        tableVentes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDetailHeader(newVal);
                loadDetails(newVal.getId());
            }
        });
    }

    /**
     * Updates the header above the detail table with the selected sale info.
     */
    private void updateDetailHeader(Vente vente) {
        lblSelectedId.setText("Vente #" + vente.getId() + " - " + vente.getDateVente().format(dtf));
        lblSelectedId.setStyle("-fx-text-fill: #009688; -fx-font-weight: bold; -fx-font-style: normal;");
    }

    /**
     * Generic Currency Cell Factory to avoid Type Erasure issues.
     * Works for any TableView row type (S).
     */
    private <S> TableCell<S, Double> createCurrencyCell() {
        return new TableCell<S, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("%.2f €", amount));
                    // Professional financial alignment
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        };
    }

    private void loadDetails(int venteId) {
        tableDetails.setItems(FXCollections.observableArrayList(venteDAO.getLignesVente(venteId)));
    }

    @FXML
    public void backToMenu(javafx.event.ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/MainView.fxml");
    }
}