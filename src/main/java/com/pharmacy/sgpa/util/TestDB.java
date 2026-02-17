package com.pharmacy.sgpa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        // EXACT setup for MAMP Port 8889
        String url = "jdbc:mysql://127.0.0.1:8889/sgpa_pharmacy?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "root";

        System.out.println("🔄 Attempting to connect...");

        try {
            // Force the driver to load
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(url, user, password);

            if (conn != null) {
                System.out.println("✅ CONNECTION SUCCESSFUL!");
                System.out.println("🚀 You are ready for your demo.");
                conn.close();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ DRIVER ERROR: MySQL Connector JAR is missing from your project!");
        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
        } catch (Exception e) {
            System.err.println("❌ UNKNOWN ERROR: " + e.getMessage());
        }
    }
}