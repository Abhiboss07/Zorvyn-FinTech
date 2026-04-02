package com.zorvyn.fintech.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption service.
 * Output format: base64(iv):base64(ciphertext+authTag)
 */
@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey currentKey;
    private final SecretKey previousKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(
            @Value("${app.encryption.key}") String hexKey,
            @Value("${app.encryption.key-previous:}") String hexKeyPrevious) {
        this.currentKey = hexToKey(hexKey);
        this.previousKey = (hexKeyPrevious != null && !hexKeyPrevious.isBlank())
                ? hexToKey(hexKeyPrevious) : null;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, currentKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String ctB64 = Base64.getEncoder().encodeToString(ciphertext);

            return ivB64 + ":" + ctB64;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            return decryptWithKey(encrypted, currentKey);
        } catch (Exception e) {
            // Try previous key for key rotation
            if (previousKey != null) {
                try {
                    return decryptWithKey(encrypted, previousKey);
                } catch (Exception ex) {
                    throw new RuntimeException("Decryption failed with both current and previous keys", ex);
                }
            }
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Re-encrypt data: decrypt with old key → encrypt with current key.
     */
    public String rotateKey(String encrypted) {
        String plaintext = decrypt(encrypted);
        return encrypt(plaintext);
    }

    private String decryptWithKey(String encrypted, SecretKey key) throws Exception {
        String[] parts = encrypted.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted format");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] ciphertext = Base64.getDecoder().decode(parts[1]);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
    }

    private SecretKey hexToKey(String hex) {
        byte[] keyBytes = hexStringToByteArray(hex);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 32 bytes (64 hex chars), got " + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
