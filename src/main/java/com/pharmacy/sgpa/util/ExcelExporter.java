package com.pharmacy.sgpa.util; // ou com.pharmacy.sgpa.service

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ExcelExporter {

    public static void exportSalesToExcel(ResultSet rs, String filePath) throws IOException, SQLException {
        // 1. Création du classeur et de la feuille
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rapport Ventes");

            // 2. Style pour l'en-tête (Gras + Fond gris)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 3. Création de l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Vente", "Date", "Montant Total (€)", "Type"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. Remplissage des données
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(rs.getInt("id"));
                row.createCell(1).setCellValue(rs.getDate("date_vente").toString());
                row.createCell(2).setCellValue(rs.getDouble("montant_total"));
                // Adaptez "type_vente" selon vos colonnes réelles en BDD
                row.createCell(3).setCellValue("Standard");
            }

            // 5. Ajuster la taille des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 6. Écriture du fichier sur le disque
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
}