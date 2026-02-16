package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.model.Fournisseur;
import com.pharmacy.sgpa.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO {

    public void addFournisseur(Fournisseur f) {
        String sql = "INSERT INTO fournisseur (nom, contact, email, adresse) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, f.getNom());
            stmt.setString(2, f.getContact());
            stmt.setString(3, f.getEmail());
            stmt.setString(4, f.getAdresse());
            stmt.executeUpdate();
            System.out.println("✅ Supplier added: " + f.getNom());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Fournisseur> getAllFournisseurs() {
        List<Fournisseur> list = new ArrayList<>();
        // Added ORDER BY to keep the dropdown sorted alphabetically
        String sql = "SELECT * FROM fournisseur ORDER BY nom ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Fournisseur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("adresse")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteFournisseur(int id) {
        String sql = "DELETE FROM fournisseur WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑 Supplier deleted: ID " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- FIX: This method now returns a List and reuses the logic above ---
    public List<Fournisseur> getAll() {
        return getAllFournisseurs();
    }
}