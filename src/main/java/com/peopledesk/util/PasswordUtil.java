package com.peopledesk.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2 (HMAC-SHA256) - built into Java, no extra library needed.
 *
 * Stored format (one text value in the password_hash column):
 *     pbkdf2_sha256$ITERATIONS$SALT_BASE64$HASH_BASE64
 *
 * - A random SALT is created for every password, so two users with the same password
 *   get different hashes.
 * - Many ITERATIONS make each guess slow, which makes brute-force attacks harder.
 * - The iteration count is stored inside the hash, so it can be increased in future.
 */
public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2_sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 310_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** Turns a plain password into a salted hash that is safe to store. */
    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Checks a plain password against a stored hash. Returns false for any problem. */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations);
            // MessageDigest.isEqual compares in constant time (avoids timing attacks).
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Spends the same time as a real check. Used when the username does not exist,
     * so an attacker cannot find valid usernames by measuring response time.
     */
    public static void spendTime(String password) {
        pbkdf2(password == null ? "" : password, new byte[SALT_BYTES], ITERATIONS);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        } finally {
            spec.clearPassword();
        }
    }

    // Handy for testing from the command line:  java -cp target/classes com.peopledesk.util.PasswordUtil mypassword
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java com.peopledesk.util.PasswordUtil <password>");
            return;
        }
        String h = hash(args[0]);
        System.out.println(h);
        System.out.println("verify(correct)  = " + verify(args[0], h));
        System.out.println("verify(wrong)    = " + verify(args[0] + "x", h));
    }
}
