package com.pharmacy.sgpa.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Encrypts the password (used during Registration)
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    // Verifies the password (used during Login)
    public static boolean checkPassword(String plainTextPassword, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2a$")) {
            System.out.println("DEBUG: Invalid Hash Format -> " + storedHash); // Add this print
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, storedHash);
    }
}