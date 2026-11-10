package com.dorm.logic.code;

import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {
    public static String hash(String password) {
        if (password == null || password.length() < 6 || password.length() > 128)
            throw new IllegalArgumentException("Password must contain 6 to 128 characters.");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2$" + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(key(password, salt));
    }
    private static byte[] key(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
        finally { spec.clearPassword(); }
    }
    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) return false;
        try {
            String[] parts = stored.split("\\$");
            return parts.length == 3 && parts[0].equals("pbkdf2") && MessageDigest.isEqual(
                    key(password, Base64.getDecoder().decode(parts[1])), Base64.getDecoder().decode(parts[2]));
        } catch (IllegalArgumentException e) { return false; }
    }
}
