package com.seveneleven.storeapp.utils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    public static String hashPasswordToSHA256(String password) {
        if (password == null) {
            return null; // Or throw an IllegalArgumentException
        }

        try {
            // 1. Get the SHA-256 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 2. Perform the hashing
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // 3. Convert the resulting byte array into a Hexadecimal String
            return bytesToHex(encodedHash);

        } catch (NoSuchAlgorithmException e) {
            // This should never happen on a standard JVM, but must be caught
            throw new RuntimeException("SHA-256 algorithm not available in this environment.", e);
        }
    }

    
     
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            // Convert byte to positive integer and then to hex
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // Pad with leading zero if necessary
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}