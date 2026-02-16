package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.StatsDAO;
import com.pharmacy.sgpa.dao.VenteDAO;
import com.pharmacy.sgpa.service.ExportService;

// JavaFX Imports
import com.pharmacy.sgpa.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// PDF Library Imports (OpenPDF)
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color; // AWT Color for PDF generation

// Standard Java Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.pharmacy.sgpa.util.NavigationUtil.showNotification;

public class RapportController {

    // --- FXML UI COMPONENTS ---
    @FXML private BarChart<String, Number> chartSales;
    @FXML private ComboBox<Integer> comboYear;

    // KPI Labels
    @FXML private Label lblTotalYear;
    @FXML private Label lblAvgMonth;
    @FXML private Label lblBestMonth;

    // --- SERVICES & DAO ---
    private StatsDAO statsDAO;
    private ExportService exportService;
    private VenteDAO venteDAO;

    // Month Names for Chart Display
    private final String[] monthNames = {"", "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
            "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

    @FXML
    public void initialize() {
        this.statsDAO = new StatsDAO();
        this.exportService = new ExportService();
        this.venteDAO = new VenteDAO();

        loadYears();

        // Listener: When year changes, reload chart and KPIs
        comboYear.setOnAction(e -> {
            if (comboYear.getValue() != null) {
                loadDashboardData(comboYear.getValue());
            }
        });
    }

    /**
     * 1. Loads available years from DB into the dropdown
     */
    private void loadYears() {
        List<Integer> years = statsDAO.getAvailableYears();
        comboYear.setItems(FXCollections.observableArrayList(years));

        if (!years.isEmpty()) {
            // Select the most recent year by default
            comboYear.getSelectionModel().selectFirst();
            loadDashboardData(comboYear.getValue());
        }
    }

    /**
     * 2. Main Refresh Method: Updates Chart AND KPI Cards
     */
    private void loadDashboardData(int year) {
        // Fetch Data
        Map<String, Double> data = statsDAO.getMonthlyRevenue(year);

        // --- A. UPDATE CHART ---
        chartSales.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus " + year);

        double totalYear = 0;
        double maxVal = 0;
        String bestMonth = "-";

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int monthIndex = Integer.parseInt(entry.getKey()); // "01" -> 1
            double amount = entry.getValue();

            // Add to Chart
            series.getData().add(new XYChart.Data<>(monthNames[monthIndex], amount));

            // Calculate KPIs
            totalYear += amount;
            if (amount > maxVal) {
                maxVal = amount;
                bestMonth = monthNames[monthIndex]; // e.g., "Déc"
            }
        }
        chartSales.getData().add(series);

        // --- B. UPDATE KPI LABELS ---
        if (lblTotalYear != null) lblTotalYear.setText(String.format("%.2f €", totalYear));
        if (lblAvgMonth != null)  lblAvgMonth.setText(String.format("%.2f €", totalYear / 12));
        if (lblBestMonth != null) lblBestMonth.setText(bestMonth);
    }

    @FXML
    public void exportPDF(ActionEvent actionEvent) {
        Integer selectedYear = comboYear.getValue();
        if (selectedYear == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        String fileName = "Rapport_Financier_" + selectedYear + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(((Node) actionEvent.getSource()).getScene().getWindow());

        if (file != null) {
            generatePDF(file, selectedYear);
        }
    }

    private void generatePDF(File file, int year) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // 1. Header
            Paragraph title = new Paragraph("SGPA PHARMA - RAPPORT ANNUEL " + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), subTitleFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // 2. Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Table Style (Teal #009688)
            Color pharmacyTeal = new Color(0, 150, 136);

            PdfPCell cell1 = new PdfPCell(new Phrase("Mois", headerFont));
            cell1.setBackgroundColor(pharmacyTeal);
            cell1.setPadding(8);

            PdfPCell cell2 = new PdfPCell(new Phrase("Chiffre d'Affaires (€)", headerFont));
            cell2.setBackgroundColor(pharmacyTeal);
            cell2.setPadding(8);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);

            table.addCell(cell1);
            table.addCell(cell2);

            // 3. Fill Table from Chart Data (Ensures WYSIWYG)
            for (XYChart.Series<String, Number> series : chartSales.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    table.addCell(new Phrase(data.getXValue(), bodyFont));

                    PdfPCell amountCell = new PdfPCell(new Phrase(String.format("%.2f €", data.getYValue().doubleValue()), bodyFont));
                    amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(amountCell);
                }
            }

            document.add(table);

            // 4. Footer
            Paragraph footer = new Paragraph("\nDocument confidentiel à usage interne uniquement.", subTitleFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();

            javafx.stage.Window owner = lblAvgMonth.getScene().getWindow();
            showNotification(owner,"Export Réussi!","Rapport PDF généré avec succès !");


        } catch (Exception e) {
            e.printStackTrace();
            javafx.stage.Window owner = lblAvgMonth.getScene().getWindow();
            showNotification(owner,"Erreur!","Erreur PDF : " + e.getMessage());
        }
    }

    @FXML
    public void exportExcel(ActionEvent event) {
        javafx.stage.Window owner = lblAvgMonth.getScene().getWindow();
        Stage stage = (Stage) chartSales.getScene().getWindow();
        // Uses your existing service, but typically you might want to filter this by year too in the future
        exportService.exportVentesToExcel(stage, javafx.collections.FXCollections.observableArrayList(venteDAO.getAllVentes()));


        showNotification(owner,"Export Réussi!","Export Excel terminé.");
    }

    @FXML
    public void backToMenu(javafx.event.ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/MainView.fxml");
    }
}