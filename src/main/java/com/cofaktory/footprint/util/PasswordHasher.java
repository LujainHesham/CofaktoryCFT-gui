package com.cofaktory.footprint.util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(saltBytes);
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        for (int i = 1; i < ITERATIONS; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }
        return Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String storedSalt, String storedHash)
            throws NoSuchAlgorithmException {
        String computedHash = hashPassword(password, storedSalt);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}