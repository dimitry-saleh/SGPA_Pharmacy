package com.pharmacy.sgpa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {


    // MAMP usually uses Port 8889
    // MAMP usually requires "root" as the password
    private static final String URL = "jdbc:mysql://localhost:8889/sgpa_pharmacy";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection connection = null;

    // Private constructor so no one can instantiate this class directly
    private DatabaseConnection() { }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load the MySQL Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Successfully connected to the database!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Database Connection Failed!");
            e.printStackTrace();
        }
        return connection;
    }
}
