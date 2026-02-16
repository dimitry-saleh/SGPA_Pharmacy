package com.pharmacy.sgpa.dao;

import com.pharmacy.sgpa.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDAO {

    /**
     * Creates a new user with a secure, hashed password.
     * @param username The unique username.
     * @param plainTextPassword The raw password (will be hashed).
     * @param role The role (ADMIN or PHARMACIEN).
     * @return true if successful, false otherwise.
     */
    public boolean createUser(String username, String plainTextPassword, String role) {
        String sql = "INSERT INTO utilisateur (username, password, role) VALUES (?, ?, ?)";

        // 1. Hash the password
        // The "12" is the log rounds (work factor), matching your existing DB format ($2a$12$...)
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            // Duplicate username error or connection issue
            e.printStackTrace();
            return false;
        }
    }
}