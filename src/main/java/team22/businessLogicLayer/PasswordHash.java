package team22.businessLogicLayer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHash {

    static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512";
    static final int PBKDF2_ITERATIONS = 120000;  // Recommended number of iterations from OWASP
    static final int PBKDF2_SALT_BYTES = 64;
    static final int PBKDF2_OUTPUT_BYTES = 48;

    static final int HASH_SECTIONS = 5;
    static final int HASH_ALGORITHM_INDEX = 0;
    static final int ITERATION_INDEX = 1;
    static final int HASH_SIZE_INDEX = 2;
    static final int SALT_INDEX = 3;
    static final int PBKDF2_INDEX = 4;

    /**
     * Securely generates a random salt for use in pbkdf2 hashing algorithm
     * @return an array of bytes containing the salt
     */
    public static byte[] generateSalt() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes = new byte[PBKDF2_SALT_BYTES];
            sr.nextBytes(bytes);
            return bytes;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Utilises PBKDF2-HMAC-SHA512 algorithm to securely (and slowly) generate a hash for a given password.
     * @param password the password to generate a hash for
     * @return the hashed password, along with metadata
     */
    public static String hashPassword(char[] password) {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(salt, password, PBKDF2_ITERATIONS);
        return "sha512:" +
                  PBKDF2_ITERATIONS + ":" +
                  hash.length + ":" +
                  Base64.getEncoder().encodeToString(salt) + ":" +
                  Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Performs PBKDF2 hashing on a password
     * @param salt the salt to use in hashing
     * @param password the password to hash
     * @param iters the number of iterations to use
     * @return the hashed password
     */
    public static byte[] pbkdf2(byte[] salt, char[] password, int iters) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            SecretKey sk = skf.generateSecret(new PBEKeySpec(password, salt, iters, PBKDF2_OUTPUT_BYTES * 8));
            return sk.getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Checks if an entered password is the same as a hashed one
     * @param password the entered password
     * @param hash the hashed password to compare against
     * @return true if equal, false otherwise
     */
    public static boolean checkPassword(char[] password, String hash) {
        String[] hashParams = hash.split(":");  // Retrieve metadata of hash

        // Initial checks to ensure hash hasn't become corrupted
        if (hashParams.length != HASH_SECTIONS) {
            return false;
        }

        if (!hashParams[HASH_ALGORITHM_INDEX].equals("sha512")) {
            return false;
        }

        // Get PBKDF2 data, convert salt and hash to byte array
        int iterations = Integer.parseInt(hashParams[ITERATION_INDEX]);
        byte[] salt = Base64.getDecoder().decode(hashParams[SALT_INDEX]);
        byte[] correctHash = Base64.getDecoder().decode(hashParams[PBKDF2_INDEX]);

        // Check for corruption
        if (Integer.parseInt(hashParams[HASH_SIZE_INDEX]) != correctHash.length) {
            return false;
        }

        // Hash entered password
        byte[] newHash = pbkdf2(salt, password, iterations);

        Arrays.fill(password, (char) 0);  // Clear password array

        // Check equality of hashes using a constant-time equality function
        return slowEquals(newHash, correctHash);
    }

    /**
     * Constant-time equality function, for comparing two hashes while preventing timing attacks
     * @param hash1 the first hash to check
     * @param hash2 the hash to check with hash1 for equality
     * @return whether the two hashes are equal
     */
    private static boolean slowEquals(byte[] hash1, byte[] hash2) {
        int diff = hash1.length ^ hash2.length;
        for (int i =0; i<hash1.length && i<hash2.length; i++) {
            diff |= hash1[i] ^ hash2[i];
        }
        return diff == 0;
    }
}
