package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.model.LignePanier;
import com.pharmacy.sgpa.model.Vente;

import com.pharmacy.sgpa.model.Medicament;
import java.util.ArrayList;
import java.util.List;

import com.pharmacy.sgpa.util.DatabaseConnection;
import java.sql.*;

public class VenteDAO {

    public boolean saveVente(Vente vente) {
        Connection conn = null;
        PreparedStatement stmtVente = null;
        PreparedStatement stmtLigne = null;
        PreparedStatement stmtStock = null;

        try {
            conn = DatabaseConnection.getConnection();

            // 1. START TRANSACTION (Turn off auto-save)
            conn.setAutoCommit(false);

            // 2. Insert the Sale Header (Vente)
            String sqlVente = "INSERT INTO vente (date_vente, montant_total, sur_ordonnance) VALUES (NOW(), ?, ?)";

            stmtVente = conn.prepareStatement(sqlVente, Statement.RETURN_GENERATED_KEYS);
            stmtVente.setDouble(1, vente.getMontantTotal());
            stmtVente.setBoolean(2, vente.isSurOrdonnance()); // <--- AJOUT
            stmtVente.executeUpdate();

            // Get the ID generated for this new sale
            ResultSet rsKey = stmtVente.getGeneratedKeys();
            int venteId = 0;
            if (rsKey.next()) {
                venteId = rsKey.getInt(1);
            }

            // 3. Process each item in the basket
            String sqlLigne = "INSERT INTO ligne_vente (vente_id, medicament_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE medicament SET stock_actuel = stock_actuel - ? WHERE id = ?";

            stmtLigne = conn.prepareStatement(sqlLigne);
            stmtStock = conn.prepareStatement(sqlStock);

            for (LignePanier item : vente.getLignes()) {
                // A. Add to Sale History
                stmtLigne.setInt(1, venteId);
                stmtLigne.setInt(2, item.getMedicament().getId());
                stmtLigne.setInt(3, item.getQuantite());
                stmtLigne.setDouble(4, item.getMedicament().getPrixPublic()); // Save price at moment of sale
                stmtLigne.addBatch(); // Queue this instruction

                // B. Subtract from Stock
                stmtStock.setInt(1, item.getQuantite());
                stmtStock.setInt(2, item.getMedicament().getId());
                stmtStock.addBatch(); // Queue this instruction
            }

            // Execute the batches
            stmtLigne.executeBatch();
            stmtStock.executeBatch();

            // 4. COMMIT (Save everything permanently)
            conn.commit();
            System.out.println("✅ Transaction Successful. Sale ID: " + venteId);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // IF ERROR: ROLLBACK (Undo everything)
            try {
                if (conn != null) conn.rollback();
                System.err.println("❌ Transaction Failed! Rolling back changes.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            // Restore AutoCommit to true for other parts of the app
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    public List<Vente> getAllVentes() {
        List<Vente> list = new ArrayList<>();
        String sql = "SELECT * FROM vente ORDER BY date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vente v = new Vente();
                v.setId(rs.getInt("id"));
                // Convert SQL Timestamp to Java LocalDateTime
                v.setDateVente(rs.getTimestamp("date_vente").toLocalDateTime());
                v.setMontantTotal(rs.getDouble("montant_total"));
                v.setSurOrdonnance(rs.getBoolean("sur_ordonnance"));
                list.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. GET ITEMS FOR A SPECIFIC SALE (For the Detail View)
    public List<LignePanier> getLignesVente(int venteId) {
        List<LignePanier> lignes = new ArrayList<>();
        String sql = "SELECT lv.quantite, lv.prix_unitaire, m.nom_commercial " +
                "FROM ligne_vente lv " +
                "JOIN medicament m ON lv.medicament_id = m.id " +
                "WHERE lv.vente_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // We create a temporary Medicament object just to hold the name
                Medicament m = new Medicament();
                m.setNomCommercial(rs.getString("nom_commercial"));
                m.setPrixPublic(rs.getDouble("prix_unitaire")); // Store historical price here

                LignePanier lp = new LignePanier(m, rs.getInt("quantite"));
                lignes.add(lp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lignes;
    }



}
