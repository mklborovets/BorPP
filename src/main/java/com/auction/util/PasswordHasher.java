package com.auction.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    
    public static String hashPassword(String password) {
        try {
            // Генеруємо сіль
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Створюємо MessageDigest з SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Додаємо сіль до MessageDigest
            md.update(salt);
            
            // Додаємо пароль і отримуємо хеш
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Комбінуємо сіль і хеш
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            // Конвертуємо в Base64 для зберігання
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Помилка при хешуванні пароля", e);
        }
    }
    
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Декодуємо збережений хеш з Base64
            byte[] combined = Base64.getDecoder().decode(storedHash);
            
            // Виділяємо сіль
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            
            // Створюємо MessageDigest і додаємо сіль
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            
            // Хешуємо введений пароль
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Порівнюємо хеші
            byte[] storedHashBytes = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, storedHashBytes, 0, storedHashBytes.length);
            
            return MessageDigest.isEqual(hashedPassword, storedHashBytes);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Помилка хешування паролю", e);
        }
    }
    
    public static boolean verify(String password, String hashedPassword) {
        String hashedInput = hash(password);
        return hashedInput.equals(hashedPassword);
    }
} 