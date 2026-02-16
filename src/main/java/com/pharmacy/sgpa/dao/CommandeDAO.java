package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.model.Commande;
import com.pharmacy.sgpa.model.Fournisseur;
import com.pharmacy.sgpa.model.LignePanier;
import com.pharmacy.sgpa.model.Medicament;
import com.pharmacy.sgpa.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeDAO {

    // 1. CREATE ORDER
    public boolean createCommande(Commande c) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // A. Insert Header
            String sqlHead = "INSERT INTO commande (fournisseur_id, date_commande, statut) VALUES (?, NOW(), 'EN_ATTENTE')";
            PreparedStatement stmtHead = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS);
            stmtHead.setInt(1, c.getFournisseur().getId());
            stmtHead.executeUpdate();

            ResultSet rs = stmtHead.getGeneratedKeys();
            int cmdId = 0;
            if (rs.next()) cmdId = rs.getInt(1);

            // B. Insert Lines (Using your specific columns)
            String sqlLine = "INSERT INTO ligne_commande (commande_id, medicament_id, quantite_commandee, prix_achat_accorde) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtLine = conn.prepareStatement(sqlLine);

            for (LignePanier line : c.getLignes()) {
                stmtLine.setInt(1, cmdId);
                stmtLine.setInt(2, line.getMedicament().getId());
                stmtLine.setInt(3, line.getQuantite()); // quantite_commandee
                stmtLine.setDouble(4, line.getMedicament().getPrixAchat()); // prix_achat_accorde
                stmtLine.addBatch();
            }
            stmtLine.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // 2. GET PENDING ORDERS

    public int getPendingOrdersCount() {
        String sql = "SELECT COUNT(*) FROM commande WHERE statut = 'EN_ATTENTE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Commande> getPendingCommandes() {
        List<Commande> list = new ArrayList<>();
        String sql = "SELECT c.id, c.date_commande, c.statut, f.id as f_id, f.nom " +
                "FROM commande c " +
                "JOIN fournisseur f ON c.fournisseur_id = f.id " +
                "WHERE c.statut = 'EN_ATTENTE' " +
                "ORDER BY c.date_commande DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                c.setStatut(rs.getString("statut"));

                Fournisseur f = new Fournisseur();
                f.setId(rs.getInt("f_id"));
                f.setNom(rs.getString("nom"));
                c.setFournisseur(f);

                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. RECEIVE ORDER (Update Stock)
    public void receptionnerCommande(int commandeId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // A. Get items to add to stock
            String sqlSelect = "SELECT medicament_id, quantite_commandee FROM ligne_commande WHERE commande_id = ?";
            PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect);
            stmtSelect.setInt(1, commandeId);
            ResultSet rs = stmtSelect.executeQuery();

            // B. Update Stock Query
            String sqlUpdateStock = "UPDATE medicament SET stock_actuel = stock_actuel + ? WHERE id = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateStock);

            while (rs.next()) {
                int medId = rs.getInt("medicament_id");
                int qty = rs.getInt("quantite_commandee");

                stmtUpdate.setInt(1, qty);
                stmtUpdate.setInt(2, medId);
                stmtUpdate.addBatch();
            }
            stmtUpdate.executeBatch();

            // C. Update Status
            String sqlStatus = "UPDATE commande SET statut = 'RECUE' WHERE id = ?";
            PreparedStatement stmtStatus = conn.prepareStatement(sqlStatus);
            stmtStatus.setInt(1, commandeId);
            stmtStatus.executeUpdate();

            conn.commit();
            System.out.println("✅ Order Received. Stock Updated.");

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }


    // Inside CommandeDAO.java

    // Dans CommandeDAO.java

    public List<LignePanier> getLignesByCommandeId(int commandeId) {
        List<LignePanier> lignes = new ArrayList<>();

        // SQL corrigé avec VOS colonnes exactes
        String sql = "SELECT m.nom_commercial, lc.quantite_commandee " +
                "FROM ligne_commande lc " +
                "JOIN medicament m ON lc.medicament_id = m.id " +
                "WHERE lc.commande_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commandeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // 1. Récupérer les données avec les bons noms de colonnes
                String nom = rs.getString("nom_commercial");
                int qte = rs.getInt("quantite_commandee");

                // 2. Créer un objet Médicament temporaire
                Medicament m = new Medicament();
                m.setNomCommercial(nom);

                // 3. Créer la ligne panier (Medicament + Quantité)
                lignes.add(new LignePanier(m, qte));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lignes;
    }
}