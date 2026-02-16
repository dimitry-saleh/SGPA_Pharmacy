package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.CommandeDAO;
import com.pharmacy.sgpa.dao.StatsDAO; // Assuming you have a DAO for stats
import com.pharmacy.sgpa.util.NavigationUtil;
import com.pharmacy.sgpa.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.stage.FileChooser;
import javafx.scene.Node;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

public class DashboardController {

    // --- FXML UI COMPONENTS ---
    @FXML private Label lblSales;
    @FXML private Label lblAlerts;
    @FXML private Label lblOrders;

    // Filters
    @FXML private ComboBox<String> comboPeriod;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;

    // Chart
    @FXML private AreaChart<String, Number> salesChart;

    // Admin Controls
    @FXML private Button btnAdminUsers; // Only visible for Admin
    @FXML private MenuButton userMenu;
    @FXML private Button btnAdminLogs;

    // --- LOGIC VARIABLES ---
    private StatsDAO statsDAO; // Use this to fetch DB data
    private boolean isSystemUpdate = false;
    private CommandeDAO cmdDAO;// FLAG TO PREVENT LISTENER LOOP

    @FXML
    public void initialize() {
        statsDAO = new StatsDAO();
        cmdDAO = new CommandeDAO();

        // 1. SETUP USER PROFILE
        setupUserProfile();

        // 2. SETUP DATE FILTERS (With the Loop Fix)
        setupDateFilters();

        // 3. LOAD INITIAL DATA
        refreshDashboard();
    }

    private void setupUserProfile() {
        String username = UserSession.getCurrentUser();
        String role = UserSession.getCurrentRole();

        // Format Name
        if (username != null && !username.isEmpty()) {
            userMenu.setText(username.substring(0, 1).toUpperCase() + username.substring(1));
        } else {
            userMenu.setText("Utilisateur");
        }

        // Show/Hide Admin Buttons based on Role
        // Note: Assurez-vous que le rôle en base est bien "ADMIN" ou "Admin"
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        // 1. Bouton Gestion Utilisateurs
        if (btnAdminUsers != null) {
            btnAdminUsers.setVisible(isAdmin);
            btnAdminUsers.setManaged(isAdmin);
        }

        // 2. Bouton Journal d'Activités (Logs)
        if (btnAdminLogs != null) {
            btnAdminLogs.setVisible(isAdmin);
            btnAdminLogs.setManaged(isAdmin);
        }
    }

    private void setupDateFilters() {
        // Populate Period ComboBox
        comboPeriod.setItems(FXCollections.observableArrayList(
                "Aujourd'hui", "Cette semaine", "Ce mois-ci", "Cette année", "Personnalisé"
        ));

        // Set default to "Ce mois-ci"
        comboPeriod.setValue("Ce mois-ci");
        updateDatesFromPeriod("Ce mois-ci");

        // LISTENER: ComboBox changes DatePickers
        comboPeriod.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("Personnalisé")) {
                isSystemUpdate = true; // LOCK
                updateDatesFromPeriod(newVal);
                refreshDashboard(); // Fetch new data
                isSystemUpdate = false; // UNLOCK
            }
        });

        // LISTENER: DatePickers change ComboBox
        javafx.beans.value.ChangeListener<LocalDate> dateListener = (obs, oldVal, newVal) -> {
            if (!isSystemUpdate) {
                comboPeriod.getSelectionModel().select("Personnalisé");
                // Optional: Auto-refresh when picking a date?
                // refreshDashboard();
            }
        };

        dateStart.valueProperty().addListener(dateListener);
        dateEnd.valueProperty().addListener(dateListener);
    }

    private void updateDatesFromPeriod(String period) {
        LocalDate now = LocalDate.now();

        switch (period) {
            case "Aujourd'hui":
                dateStart.setValue(now);
                dateEnd.setValue(now);
                break;
            case "Cette semaine":
                dateStart.setValue(now.with(DayOfWeek.MONDAY));
                dateEnd.setValue(now.with(DayOfWeek.SUNDAY));
                break;
            case "Ce mois-ci":
                dateStart.setValue(now.with(TemporalAdjusters.firstDayOfMonth()));
                dateEnd.setValue(now.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "Cette année":
                dateStart.setValue(now.with(TemporalAdjusters.firstDayOfYear()));
                dateEnd.setValue(now.with(TemporalAdjusters.lastDayOfYear()));
                break;
        }
    }

    // --- DATA LOADING ---

    @FXML
    public void handleFilterUpdate() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        LocalDate start = dateStart.getValue();
        LocalDate end = dateEnd.getValue();

        if (start == null || end == null) return;

        // 1. Fetch KPIs
        // (Replace these calls with your actual DAO methods)
        double totalSales = statsDAO.getTotalRevenue(start, end);
        int alerts = statsDAO.getCountLowStock();
        int orders = cmdDAO.getPendingOrdersCount();

        // 2. Update UI
        lblSales.setText(String.format("%.2f €", totalSales));
        lblAlerts.setText(alerts + " Articles");
        lblOrders.setText(String.valueOf(orders));

        // Color the Alert Text if critical
        if (alerts > 0) {
            lblAlerts.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        } else {
            lblAlerts.setStyle("-fx-text-fill: #37474F;");
        }

        // 3. Update Chart
        updateChart(start, end);
    }

    private void updateChart(LocalDate start, LocalDate end) {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes");

        // Fetch data map (Date -> Revenue)
        Map<String, Double> data = statsDAO.getDailySales(start, end);

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        salesChart.getData().add(series);
    }

    // --- NAVIGATION ACTIONS ---

    // ... existing code ...

    /**
     * Clicking "Stock in Danger" sends user to the Order Creation page
     */
    @FXML
    public void goToCommande(javafx.scene.input.MouseEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/CommandeView.fxml");
    }

    /**
     * Clicking "Pending Orders" sends user to the Reception page
     * (Note: It opens CommandeView. You will manually click the 2nd tab)
     */
    @FXML
    public void goToReception(javafx.scene.input.MouseEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/CommandeView.fxml");
    }

    // ... existing code ...


    @FXML
    public void goToVente(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/VenteView.fxml");
    }

    @FXML
    public void goToStock(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/MainView.fxml");
    }

    @FXML
    public void goToHistory(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/HistoriqueView.fxml");
    }

    @FXML
    public void goToReports(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/RapportView.fxml");
    }

    @FXML
    public void handleCreateUser(ActionEvent event) {
        try {
            // Get the current window (Dashboard) to act as the owner
            Window owner = userMenu.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
            Parent root = loader.load();

            // Create a NEW stage for the popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Nouvel Utilisateur");

            // --- THE FULL SCREEN FIXES ---
            popupStage.initOwner(owner); // Anchors it to Dashboard
            popupStage.initModality(Modality.APPLICATION_MODAL); // Blocks Dashboard until closed

            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.showAndWait(); // Wait here until the popup is closed

        } catch (IOException e) {
            e.printStackTrace();
            // Since we are in a Menu, use userMenu for the alert owner
            NavigationUtil.showNotification(userMenu.getScene().getWindow(), "Erreur", "Impossible d'ouvrir la fenêtre d'inscription.");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Clean the session first
        UserSession.cleanUserSession();

        try {
            // 1. Get the window safely using the FXML component 'userMenu'
            // instead of casting the event source.
            Stage stage = (Stage) userMenu.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            // 2. Switch the scene
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSmartExport(ActionEvent event) {
        // 1. Validation: Ensure dates are selected
        if (dateStart.getValue() == null || dateEnd.getValue() == null) {
            showError("Veuillez sélectionner une période valide.");
            return;
        }

        // 2. Fetch Data from DAO based on current filters
        Map<String, Double> salesData = statsDAO.getDailySales(dateStart.getValue(), dateEnd.getValue());

        if (salesData.isEmpty()) {
            showError("Aucune donnée à exporter pour cette période.");
            return;
        }

        // 3. Configure FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le Rapport Excel");
        String defaultName = "Rapport_Ventes_" + dateStart.getValue() + "_au_" + dateEnd.getValue() + ".xlsx";
        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));

        // 4. Show Save Dialog
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            generateExcel(file, salesData);
        }
    }

    private void generateExcel(File file, Map<String, Double> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rapport Ventes");

            // --- STYLES ---
            // 1. Header Style (Bold, Teal Background, White Text)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 2. Currency Style
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0.00 \"€\""));

            // --- TITLE ROW ---
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Rapport de Ventes: " + dateStart.getValue() + " au " + dateEnd.getValue());
            // Make title bold
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // --- HEADER ROW ---
            Row headerRow = sheet.createRow(2); // Skip a row for spacing

            Cell cellDate = headerRow.createCell(0);
            cellDate.setCellValue("Date");
            cellDate.setCellStyle(headerStyle);

            Cell cellRevenue = headerRow.createCell(1);
            cellRevenue.setCellValue("Chiffre d'Affaires");
            cellRevenue.setCellStyle(headerStyle);

            // --- DATA ROWS ---
            int rowNum = 3;
            double totalSum = 0;

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(entry.getKey()); // Date string

                Cell valCell = row.createCell(1);
                valCell.setCellValue(entry.getValue());
                valCell.setCellStyle(currencyStyle);

                totalSum += entry.getValue();
            }

            // --- TOTAL ROW ---
            Row totalRow = sheet.createRow(rowNum + 1);
            Cell lblTotal = totalRow.createCell(0);
            lblTotal.setCellValue("TOTAL PÉRIODE");
            lblTotal.setCellStyle(headerStyle);

            Cell valTotal = totalRow.createCell(1);
            valTotal.setCellValue(totalSum);
            valTotal.setCellStyle(currencyStyle); // Apply currency format

            // --- AUTO SIZE COLUMNS ---
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // --- WRITE TO FILE ---
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            // Success Message

            javafx.stage.Window owner = dateStart.getScene().getWindow();

            // Call the safe notification utility
            com.pharmacy.sgpa.util.NavigationUtil.showNotification(
                    owner,
                    "Export Réussi",
                    "Le fichier Excel a été généré avec succès !"
            );

        } catch (IOException e) {
            e.printStackTrace();

            // Get owner again for the error path
            // Inside handleSmartExport
            javafx.stage.Window owner = dateStart.getScene().getWindow();

            // Replace showError with safe notification
            com.pharmacy.sgpa.util.NavigationUtil.showNotification(
                    owner,
                    "Erreur d'Exportation",
                    "Erreur lors de l'exportation Excel : " + e.getMessage()
            );
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    public void goToLogs(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/LogsView.fxml");
    }
}