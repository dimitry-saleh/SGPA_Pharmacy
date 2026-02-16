package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class StatsDAO {

    /**
     * USED BY PREDICTION SERVICE
     * Returns total quantity sold of a specific drug in the last X days.
     */
    public int getSalesVolume(int medicamentId, int daysLookBack) {
        int totalSold = 0;
        // SQL: Join Sales Lines with Sales Header to filter by Date
        String sql = "SELECT SUM(lv.quantite) " +
                "FROM ligne_vente lv " +
                "JOIN vente v ON lv.vente_id = v.id " +
                "WHERE lv.medicament_id = ? " +
                "AND v.date_vente >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicamentId);
            stmt.setInt(2, daysLookBack);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalSold = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalSold;
    }

    /**
     * KPI 1: Calculate Total Revenue between two dates
     */
    public double getTotalRevenue(LocalDate start, LocalDate end) {
        double total = 0.0;
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM vente WHERE date_vente BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * KPI 2: Count number of products where Stock <= Minimum Threshold
     */
    public int getCountLowStock() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM medicament WHERE stock_actuel <= seuil_min";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * KPI 3: Count pending orders
     */
    public int getCountPendingOrders() {
        int count = 0;
        // Note: Make sure 'commande' table exists, otherwise this returns 0 safely
        String sql = "SELECT COUNT(*) FROM commande WHERE statut NOT IN ('RECU', 'ANNULE')";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            // Table might not exist yet
            return 0;
        }
        return count;
    }

    /**
     * CHART DATA: Get daily revenue for the area chart
     */
    public Map<String, Double> getDailySales(LocalDate start, LocalDate end) {
        Map<String, Double> data = new TreeMap<>(); // TreeMap sorts by Date key automatically

        String sql = "SELECT DATE(date_vente) as jour, SUM(montant_total) as total " +
                "FROM vente " +
                "WHERE date_vente BETWEEN ? AND ? " +
                "GROUP BY jour ORDER BY jour ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("jour"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * RAPPORT PDF DATA: Get Monthly revenue for the last 12 months
     */
    public Map<String, Double> getMonthlyRevenue() {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(date_vente, '%Y-%m') as mois, SUM(montant_total) as total " +
                "FROM vente " +
                "GROUP BY mois ORDER BY mois ASC LIMIT 12";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.put(rs.getString("mois"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }


    // 1. Get all years that have sales (for the dropdown)
    public java.util.List<Integer> getAvailableYears() {
        java.util.List<Integer> years = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(date_vente) as annee FROM vente ORDER BY annee DESC";

        try (java.sql.Connection conn = com.pharmacy.sgpa.util.DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                years.add(rs.getInt("annee"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Fallback: If no sales exist, at least show the current year
        if (years.isEmpty()) {
            years.add(java.time.LocalDate.now().getYear());
        }

        return years;
    }

    // 2. Update this method to accept a 'year' parameter
    public java.util.Map<String, Double> getMonthlyRevenue(int year) {
        java.util.Map<String, Double> data = new java.util.LinkedHashMap<>();

        // Filter by the specific year
        String sql = "SELECT DATE_FORMAT(date_vente, '%m') as mois, SUM(montant_total) as total " +
                "FROM vente " +
                "WHERE YEAR(date_vente) = ? " +
                "GROUP BY mois ORDER BY mois ASC";

        try (java.sql.Connection conn = com.pharmacy.sgpa.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, year);
            java.sql.ResultSet rs = stmt.executeQuery();

            // Initialize all months to 0.0 so the chart looks complete (Jan-Dec)
            for (int i = 1; i <= 12; i++) {
                data.put(String.format("%02d", i), 0.0);
            }

            // Fill in actual data
            while (rs.next()) {
                data.put(rs.getString("mois"), rs.getDouble("total"));
            }

        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
}