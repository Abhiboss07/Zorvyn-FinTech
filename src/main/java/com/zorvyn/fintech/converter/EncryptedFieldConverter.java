package com.zorvyn.fintech.converter;

import com.zorvyn.fintech.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that transparently encrypts on persist/merge
 * and decrypts on load using AES-256-GCM via EncryptionService.
 */
@Converter
@Component
public class EncryptedFieldConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    /**
     * Spring injects the EncryptionService via constructor.
     * We store it in a static field because JPA instantiates converters
     * without Spring context awareness.
     */
    public EncryptedFieldConverter(EncryptionService encryptionService) {
        EncryptedFieldConverter.encryptionService = encryptionService;
    }

    public EncryptedFieldConverter() {
        // No-arg constructor required by JPA
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        if (encryptionService == null) {
            return attribute; // Graceful fallback during startup/migration
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        if (encryptionService == null) {
            return dbData;
        }
        try {
            return encryptionService.decrypt(dbData);
        } catch (Exception e) {
            // If decryption fails (e.g., plaintext seed data), return as-is
            return dbData;
        }
    }
}
