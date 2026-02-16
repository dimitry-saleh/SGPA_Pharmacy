package com.pharmacy.sgpa.util;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private String username;
    private String role;

    // Private constructor to prevent direct instantiation
    private UserSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    // --- 1. SESSION INITIALIZATION ---
    public static void setSession(int userId, String username, String role) {
        if (instance == null) {
            instance = new UserSession(userId, username, role);
        }
    }

    // --- 2. STATIC HELPER METHODS (Used by Controllers) ---

    // Allows calling UserSession.getCurrentUser() directly
    public static String getCurrentUser() {
        return (instance != null) ? instance.username : null;
    }

    // Allows calling UserSession.getCurrentRole() directly
    public static String getCurrentRole() {
        return (instance != null) ? instance.role : null;
    }

    // Allows calling UserSession.getCurrentUserId() directly
    public static int getCurrentUserId() {
        return (instance != null) ? instance.userId : 0;
    }

    // --- 3. CLEANUP (Logout) ---
    public static void cleanUserSession() {
        instance = null; // Wipes the current user from memory
    }

    // --- 4. STANDARD SINGLETON ACCESS (Optional but good to keep) ---
    public static UserSession getInstance() {
        return instance;
    }

    // Instance Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}