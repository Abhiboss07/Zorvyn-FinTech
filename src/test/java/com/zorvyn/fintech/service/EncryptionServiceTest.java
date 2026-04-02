package com.zorvyn.fintech.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    // 32 bytes hex key
    private static final String TEST_KEY = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService(TEST_KEY, "");
    }

    @Test
    void encrypt_decrypt_roundTrip() {
        String original = "ACCT-1234-5678-9012";
        String encrypted = encryptionService.encrypt(original);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        assertTrue(encrypted.contains(":"), "Encrypted format should contain colon separator");

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_producesDifferentCiphertext_forSameInput() {
        String original = "SensitiveData123";
        String encrypted1 = encryptionService.encrypt(original);
        String encrypted2 = encryptionService.encrypt(original);

        // Due to random IV, same plaintext should produce different ciphertext
        assertNotEquals(encrypted1, encrypted2);

        // But both should decrypt back to the same value
        assertEquals(original, encryptionService.decrypt(encrypted1));
        assertEquals(original, encryptionService.decrypt(encrypted2));
    }

    @Test
    void decrypt_tampered_throws() {
        String encrypted = encryptionService.encrypt("TestData");
        String tampered = encrypted + "tamper";

        assertThrows(Exception.class, () -> encryptionService.decrypt(tampered));
    }

    @Test
    void decrypt_invalidFormat_throws() {
        assertThrows(Exception.class, () -> encryptionService.decrypt("not-valid-encrypted-data"));
    }

    @Test
    void keyRotation_works() {
        String oldKey = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789";
        EncryptionService oldService = new EncryptionService(oldKey, "");
        String encrypted = oldService.encrypt("RotateMe");

        // Create new service with new key and old key as previous
        String newKey = TEST_KEY;
        EncryptionService newService = new EncryptionService(newKey, oldKey);

        // Should be able to decrypt with old key
        String decrypted = newService.decrypt(encrypted);
        assertEquals("RotateMe", decrypted);

        // Re-encrypt with new key
        String reEncrypted = newService.rotateKey(encrypted);
        assertNotEquals(encrypted, reEncrypted);

        // Now decrypt with just the new key
        EncryptionService finalService = new EncryptionService(newKey, "");
        assertEquals("RotateMe", finalService.decrypt(reEncrypted));
    }

    @Test
    void encrypt_emptyString() {
        String encrypted = encryptionService.encrypt("");
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals("", decrypted);
    }

    @Test
    void encrypt_specialCharacters() {
        String original = "Héllo! @#$%^&*() 🎉 café résumé";
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
}
