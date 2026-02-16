package com.pharmacy.sgpa.service;

import com.pharmacy.sgpa.model.Vente;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

public class ExportService {

    public void exportVentesToExcel(Stage stage, ObservableList<Vente> sales) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le rapport");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel (CSV)", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {

                // 1. Write Header (The columns)
                writer.println("ID Vente;Date;Montant Total;Sur Ordonnance");

                // 2. Write Data
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                for (Vente v : sales) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(v.getId()).append(";");
                    sb.append(v.getDateVente().format(fmt)).append(";");
                    sb.append(String.format("%.2f", v.getMontantTotal()).replace(".", ",")).append(";"); // Excel prefers commas in Europe
                    sb.append(v.isSurOrdonnance() ? "OUI" : "NON");

                    writer.println(sb.toString());
                }

                System.out.println("Export réussi !");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
