package com.auction.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void testHashPassword() {
        // Перевірка, що метод не повертає null
        String password = "TestPassword123!";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword, "Хешований пароль не повинен бути null");
        assertFalse(hashedPassword.isEmpty(), "Хешований пароль не повинен бути порожнім");
        
        // Перевірка, що хеш відрізняється від оригінального пароля
        assertNotEquals(password, hashedPassword, "Хеш не повинен дорівнювати оригінальному паролю");
    }
    
    @Test
    void testVerifyPassword() {
        // Перевірка, що правильний пароль верифікується
        String password = "TestPassword123!";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword), 
                "Правильний пароль повинен верифікуватися");
        
        // Перевірка, що неправильний пароль не верифікується
        assertFalse(PasswordHasher.verifyPassword("WrongPassword123!", hashedPassword), 
                "Неправильний пароль не повинен верифікуватися");
    }
    
    @Test
    void testHashMethod() {
        // Перевірка, що метод hash не повертає null
        String password = "TestPassword123!";
        String hashedPassword = PasswordHasher.hash(password);
        
        assertNotNull(hashedPassword, "Хешований пароль не повинен бути null");
        assertFalse(hashedPassword.isEmpty(), "Хешований пароль не повинен бути порожнім");
        
        // Перевірка, що хеш відрізняється від оригінального пароля
        assertNotEquals(password, hashedPassword, "Хеш не повинен дорівнювати оригінальному паролю");
    }
    
    @Test
    void testVerifyMethod() {
        // Перевірка, що правильний пароль верифікується
        String password = "TestPassword123!";
        String hashedPassword = PasswordHasher.hash(password);
        
        assertTrue(PasswordHasher.verify(password, hashedPassword), 
                "Правильний пароль повинен верифікуватися");
        
        // Перевірка, що неправильний пароль не верифікується
        assertFalse(PasswordHasher.verify("WrongPassword123!", hashedPassword), 
                "Неправильний пароль не повинен верифікуватися");
    }
    
    @Test
    void testConsistencyOfHash() {
        // Перевірка, що один і той самий пароль дає однаковий хеш при використанні методу hash
        String password = "TestPassword123!";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);
        
        assertEquals(hash1, hash2, "Хеші для одного й того ж пароля повинні бути однаковими при використанні методу hash");
    }
    
    @Test
    void testDifferentHashesForSamePassword() {
        // Перевірка, що один і той самий пароль дає різні хеші при використанні методу hashPassword (через випадкову сіль)
        String password = "TestPassword123!";
        String hash1 = PasswordHasher.hashPassword(password);
        String hash2 = PasswordHasher.hashPassword(password);
        
        assertNotEquals(hash1, hash2, "Хеші для одного й того ж пароля повинні бути різними при використанні методу hashPassword");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"Short1!", "MediumPass123!", "VeryLongPasswordWithSpecialChars123!@#"})
    void testDifferentPasswordLengths(String password) {
        // Перевірка, що паролі різної довжини успішно хешуються і верифікуються
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword), 
                "Пароль повинен верифікуватися незалежно від його довжини");
    }
    
    @Test
    void testNullPassword() {
        // Перевірка обробки null-значень
        assertThrows(Exception.class, () -> PasswordHasher.hashPassword(null), 
                "Хешування null-пароля повинно викликати виняток");
        
        assertThrows(Exception.class, () -> PasswordHasher.hash(null), 
                "Хешування null-пароля повинно викликати виняток");
    }
    
    @Test
    void testInvalidHashFormat() {
        // Перевірка обробки невалідного формату хешу
        assertFalse(PasswordHasher.verifyPassword("password", "invalid-hash-format"), 
                "Верифікація з невалідним форматом хешу повинна повертати false");
    }
}
