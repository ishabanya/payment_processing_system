package com.enterprise.payment.service;

import com.enterprise.payment.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for data encryption and decryption for sensitive data
 */
@Service
@Slf4j
public class EncryptionService extends BaseService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${app.encryption.key:mySecretEncryptionKey1234567890123456}")
    private String encryptionKey;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypt sensitive data
     */
    public String encrypt(String plaintext) {
        logMethodEntry("encrypt", "***");
        
        if (plaintext == null || plaintext.isEmpty()) {
            throw new ValidationException("Plaintext cannot be null or empty");
        }
        
        try {
            SecretKey secretKey = getSecretKey();
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            String encrypted = Base64.getEncoder().encodeToString(encryptedWithIv);
            
            log.debug("Data encrypted successfully");
            logMethodExit("encrypt", "encrypted");
            return encrypted;
            
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new ValidationException("Failed to encrypt data");
        }
    }

    /**
     * Decrypt sensitive data
     */
    public String decrypt(String encryptedData) {
        logMethodEntry("decrypt", "***");
        
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new ValidationException("Encrypted data cannot be null or empty");
        }
        
        try {
            SecretKey secretKey = getSecretKey();
            
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] decryptedData = cipher.doFinal(encrypted);
            String decrypted = new String(decryptedData, StandardCharsets.UTF_8);
            
            log.debug("Data decrypted successfully");
            logMethodExit("decrypt", "decrypted");
            return decrypted;
            
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new ValidationException("Failed to decrypt data");
        }
    }

    /**
     * Encrypt payment method data (card numbers, etc.)
     */
    public String encryptPaymentData(String paymentData) {
        logMethodEntry("encryptPaymentData", "***");
        
        String encrypted = encrypt(paymentData);
        
        auditLog("PAYMENT_DATA_ENCRYPTED", "SECURITY", null, 
                "Payment data encrypted");
        
        logMethodExit("encryptPaymentData", "encrypted");
        return encrypted;
    }

    /**
     * Decrypt payment method data
     */
    public String decryptPaymentData(String encryptedPaymentData) {
        logMethodEntry("decryptPaymentData", "***");
        
        String decrypted = decrypt(encryptedPaymentData);
        
        auditLog("PAYMENT_DATA_DECRYPTED", "SECURITY", null, 
                "Payment data decrypted");
        
        logMethodExit("decryptPaymentData", "decrypted");
        return decrypted;
    }

    /**
     * Hash sensitive data (one-way)
     */
    public String hashData(String data) {
        logMethodEntry("hashData", "***");
        
        if (data == null || data.isEmpty()) {
            throw new ValidationException("Data to hash cannot be null or empty");
        }
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            String hashed = Base64.getEncoder().encodeToString(hashedBytes);
            
            log.debug("Data hashed successfully");
            logMethodExit("hashData", "hashed");
            return hashed;
            
        } catch (Exception e) {
            log.error("Hashing failed", e);
            throw new ValidationException("Failed to hash data");
        }
    }

    /**
     * Generate secure random key for encryption
     */
    public String generateSecureKey() {
        logMethodEntry("generateSecureKey");
        
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // AES-256
            SecretKey secretKey = keyGenerator.generateKey();
            String key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
            log.info("Secure key generated");
            logMethodExit("generateSecureKey", "key_generated");
            return key;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate secure key", e);
            throw new ValidationException("Failed to generate secure key");
        }
    }

    /**
     * Mask sensitive data for logging (show only first and last few characters)
     */
    public String maskSensitiveData(String sensitiveData) {
        if (sensitiveData == null || sensitiveData.length() <= 8) {
            return "****";
        }
        
        int length = sensitiveData.length();
        String prefix = sensitiveData.substring(0, 2);
        String suffix = sensitiveData.substring(length - 2);
        String masked = prefix + "*".repeat(length - 4) + suffix;
        
        return masked;
    }

    /**
     * Validate if data is encrypted (basic check)
     */
    public boolean isEncrypted(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(data);
            // If it's a valid base64 string and has reasonable length, likely encrypted
            return data.length() > 16;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Generate secure random token
     */
    public String generateSecureToken(int length) {
        logMethodEntry("generateSecureToken", length);
        
        if (length <= 0) {
            throw new ValidationException("Token length must be positive");
        }
        
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        log.debug("Secure token generated with length: {}", length);
        logMethodExit("generateSecureToken", "token_generated");
        return token;
    }

    /**
     * Generate secure random number
     */
    public long generateSecureRandomNumber() {
        return secureRandom.nextLong();
    }

    /**
     * Generate secure random number within range
     */
    public int generateSecureRandomNumber(int min, int max) {
        if (min >= max) {
            throw new ValidationException("Min value must be less than max value");
        }
        
        return secureRandom.nextInt(max - min) + min;
    }

    // Private helper methods

    private SecretKey getSecretKey() {
        // Ensure key is exactly 32 bytes for AES-256
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        
        if (keyBytes.length >= 32) {
            System.arraycopy(keyBytes, 0, key, 0, 32);
        } else {
            System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
            // Pad with zeros if key is shorter
            for (int i = keyBytes.length; i < 32; i++) {
                key[i] = 0;
            }
        }
        
        return new SecretKeySpec(key, ALGORITHM);
    }
}