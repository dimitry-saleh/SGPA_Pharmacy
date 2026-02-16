package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class DashboardDAO {

    /**
     * Calculates the total sales revenue between two dates (inclusive).
     * Used for the "Chiffre d'Affaires" KPI card.
     */
    public double getTotalSalesBetween(LocalDate start, LocalDate end) {
        double total = 0.0;
        // Using DATE() ensures we ignore the time component if stored as DATETIME
        String sql = "SELECT SUM(montant_total) FROM vente WHERE DATE(date_vente) BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
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
     * Retrieves sales grouped by day for the specific period.
     * Used to populate the "Evolution des Ventes" AreaChart.
     * Returns a TreeMap to ensure dates are automatically sorted.
     */
    public Map<String, Double> getDailySalesStats(LocalDate start, LocalDate end) {
        Map<String, Double> stats = new TreeMap<>();

        String sql = "SELECT DATE(date_vente) as jour, SUM(montant_total) as total " +
                "FROM vente " +
                "WHERE DATE(date_vente) BETWEEN ? AND ? " +
                "GROUP BY DATE(date_vente) " +
                "ORDER BY jour ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String day = rs.getString("jour");
                    double amount = rs.getDouble("total");
                    stats.put(day, amount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * KPI: Counts products where current stock is below minimum threshold.
     */
    public int getLowStockCount() {
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
     * KPI: Counts orders that are currently pending.
     */
    public int getPendingOrdersCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM commande WHERE statut = 'EN_ATTENTE'";

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

    // Dans DashboardDAO.java

    public ResultSet getSalesResultSet(java.time.LocalDate start, java.time.LocalDate end) throws SQLException {
        String sql = "SELECT * FROM vente WHERE DATE(date_vente) BETWEEN ? AND ? ORDER BY date_vente DESC";

        // Note: On ne ferme pas la connexion ici car le ResultSet en a besoin pour être lu par l'exportateur.
        // L'exportateur devra gérer la fermeture ou on utilisera une approche différente si besoin.
        // Pour faire simple ici, on ouvre une nouvelle connexion dédiée :

        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setDate(1, java.sql.Date.valueOf(start));
        pstmt.setDate(2, java.sql.Date.valueOf(end));

        return pstmt.executeQuery();
    }
}