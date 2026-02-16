package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.model.LogEntry; // Make sure you have created this class
import com.pharmacy.sgpa.util.DatabaseConnection;
import com.pharmacy.sgpa.util.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoggerDAO {

    /**
     * WRITE: Saves an action to the database.
     * Usage: LoggerDAO.log("Connexion réussie");
     */
    public static void log(String action) {
        // Retrieve the current session
        UserSession session = UserSession.getInstance();

        // Safety check: If nobody is logged in (e.g., system startup), don't log user actions
        if (session == null) return;

        String sql = "INSERT INTO journal_activite (utilisateur_id, action, date_action) VALUES (?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, session.getUserId());
            stmt.setString(2, action);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'enregistrement du log : " + e.getMessage());
        }
    }

    /**
     * READ: Fetches the history of actions for the Admin Dashboard.
     * Joins with 'utilisateur' table to get the actual username instead of just the ID.
     */
    public List<LogEntry> getAllLogs() {
        List<LogEntry> logs = new ArrayList<>();

        // SQL Query: Join tables to get Username + Action + Date, ordered by newest first
        String sql = "SELECT j.id, u.username, j.action, j.date_action " +
                "FROM journal_activite j " +
                "JOIN utilisateur u ON j.utilisateur_id = u.id " +
                "ORDER BY j.date_action DESC LIMIT 100"; // Limit to 100 to prevent lag

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(new LogEntry(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getTimestamp("date_action").toLocalDateTime()
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}