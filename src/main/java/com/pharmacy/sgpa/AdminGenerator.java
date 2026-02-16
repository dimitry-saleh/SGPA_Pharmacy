package com.pharmacy.sgpa;

import com.pharmacy.sgpa.util.DatabaseConnection;
import com.pharmacy.sgpa.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AdminGenerator {
    public static void main(String[] args) {

        String username = "admin";
        String password = "admin123"; // <--- The plain text password you want
        String role = "ADMIN";

        // 1. Hash the password using your utility
        String hashedPassword = PasswordUtil.hashPassword(password);
        System.out.println("Generated Hash: " + hashedPassword);

        // 2. Insert into Database
        String sql = "INSERT INTO utilisateur (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);

            stmt.executeUpdate();
            System.out.println("✅ SUCCESS: Admin user created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ ERROR: Could not create admin.");
        }
    }
}
