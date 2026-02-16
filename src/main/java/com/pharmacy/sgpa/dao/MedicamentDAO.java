package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.model.Medicament;
import com.pharmacy.sgpa.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicamentDAO {

    // 1. CREATE: Add a new drug to the database
    public void addMedicament(Medicament m) {
        String sql = "INSERT INTO medicament (nom_commercial, principe_actif, forme_galenique, dosage, " +
                "prix_achat, prix_public, stock_actuel, seuil_min, date_peremption, necessite_ordonnance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getNomCommercial());
            stmt.setString(2, m.getPrincipeActif());
            stmt.setString(3, m.getFormeGalenique());
            stmt.setString(4, m.getDosage());
            stmt.setDouble(5, m.getPrixAchat());
            stmt.setDouble(6, m.getPrixPublic());
            stmt.setInt(7, m.getStockActuel());
            stmt.setInt(8, m.getSeuilMin());
            // Convert Java LocalDate to SQL Date
            stmt.setDate(9, Date.valueOf(m.getDatePeremption()));
            stmt.setBoolean(10, m.isNecessiteOrdonnance());

            stmt.executeUpdate();
            System.out.println("✅ Medicament added successfully: " + m.getNomCommercial());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. READ: Get all drugs (for the TableView later)
    public List<Medicament> getAllMedicaments() {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Medicament m = new Medicament();
                m.setId(rs.getInt("id"));
                m.setNomCommercial(rs.getString("nom_commercial"));
                m.setPrincipeActif(rs.getString("principe_actif"));
                m.setFormeGalenique(rs.getString("forme_galenique"));
                m.setDosage(rs.getString("dosage"));
                m.setPrixAchat(rs.getDouble("prix_achat"));
                m.setPrixPublic(rs.getDouble("prix_public"));
                m.setStockActuel(rs.getInt("stock_actuel"));
                m.setSeuilMin(rs.getInt("seuil_min"));

                // --- THE MISSING LINE ---
                // This is critical for the "Edit" form to know which supplier is selected
                m.setFournisseurId(rs.getInt("fournisseur_id"));
                // ------------------------

                if (rs.getDate("date_peremption") != null) {
                    m.setDatePeremption(rs.getDate("date_peremption").toLocalDate());
                }

                m.setNecessiteOrdonnance(rs.getBoolean("necessite_ordonnance"));

                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // 3. UPDATE
    public void updateMedicament(Medicament m) {
        // 1. Added "fournisseur_id=?" to the SQL string
        String sql = "UPDATE medicament SET nom_commercial=?, principe_actif=?, forme_galenique=?, dosage=?, " +
                "prix_achat=?, prix_public=?, stock_actuel=?, seuil_min=?, date_peremption=?, necessite_ordonnance=?, " +
                "fournisseur_id=? " + // <--- NEW FIELD
                "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getNomCommercial());
            stmt.setString(2, m.getPrincipeActif());
            stmt.setString(3, m.getFormeGalenique());
            stmt.setString(4, m.getDosage());
            stmt.setDouble(5, m.getPrixAchat());
            stmt.setDouble(6, m.getPrixPublic());
            stmt.setInt(7, m.getStockActuel());
            stmt.setInt(8, m.getSeuilMin());

            // Handle Date conversion safely (check for nulls if necessary, though m.getDatePeremption() usually returns a value)
            if (m.getDatePeremption() != null) {
                stmt.setDate(9, java.sql.Date.valueOf(m.getDatePeremption()));
            } else {
                stmt.setNull(9, java.sql.Types.DATE);
            }

            stmt.setBoolean(10, m.isNecessiteOrdonnance());

            // 2. Set the Supplier ID
            stmt.setInt(11, m.getFournisseurId()); // <--- NEW PARAMETER

            // 3. The ID is now at position 12
            stmt.setInt(12, m.getId());

            stmt.executeUpdate();
            System.out.println("✅ Medicament updated: " + m.getNomCommercial());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. DELETE
    public void deleteMedicament(int id) {
        String sql = "DELETE FROM medicament WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Medicament deleted ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Get drugs belonging to a specific supplier
    public List<Medicament> getMedicamentsByFournisseur(int fournisseurId) {
        List<Medicament> list = new ArrayList<>();
        // Note: We check fournisseur_id = ?
        String sql = "SELECT * FROM medicament WHERE fournisseur_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fournisseurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Medicament m = new Medicament();
                m.setId(rs.getInt("id"));
                m.setNomCommercial(rs.getString("nom_commercial"));
                m.setStockActuel(rs.getInt("stock_actuel"));
                m.setSeuilMin(rs.getInt("seuil_min"));
                m.setPrixAchat(rs.getDouble("prix_achat"));
                m.setFournisseurId(rs.getInt("fournisseur_id")); // Store it
                // ... map other fields if needed ...
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}