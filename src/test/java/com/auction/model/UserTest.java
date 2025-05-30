package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    private User user;
    
    @BeforeEach
    void setUp() {
        // Ініціалізація об'єкта для тестування перед кожним тестом
        user = new User();
    }
    
    @Test
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 1L;
        
        // Act
        user.setId(expectedId);
        Long actualId = user.getId();
        
        // Assert
        assertEquals(expectedId, actualId, "getId повинен повертати те саме значення, що було встановлено через setId");
    }
    
    @Test
    void testSetAndGetUsername() {
        // Arrange
        String expectedUsername = "testuser";
        
        // Act
        user.setUsername(expectedUsername);
        String actualUsername = user.getUsername();
        
        // Assert
        assertEquals(expectedUsername, actualUsername, "getUsername повинен повертати те саме значення, що було встановлено через setUsername");
    }
    
    @Test
    void testSetAndGetEmail() {
        // Arrange
        String expectedEmail = "test@example.com";
        
        // Act
        user.setEmail(expectedEmail);
        String actualEmail = user.getEmail();
        
        // Assert
        assertEquals(expectedEmail, actualEmail, "getEmail повинен повертати те саме значення, що було встановлено через setEmail");
    }
    
    @Test
    void testSetAndGetPasswordHash() {
        // Arrange
        String expectedPasswordHash = "hashedpassword123";
        
        // Act
        user.setPassword(expectedPasswordHash);
        String actualPasswordHash = user.getPassword();
        
        // Assert
        assertEquals(expectedPasswordHash, actualPasswordHash, "getPassword повинен повертати те саме значення, що було встановлено через setPassword");
    }
    
    @Test
    void testSetAndGetRole() {
        // Arrange
        String expectedRole = "ADMIN";
        
        // Act
        user.setRole(expectedRole);
        String actualRole = user.getRole();
        
        // Assert
        assertEquals(expectedRole, actualRole, "getRole повинен повертати те саме значення, що було встановлено через setRole");
    }
    
    @Test
    void testSetAndGetBalance() {
        // Arrange
        double expectedBalance = 1000.50;
        
        // Act
        user.setBalance(expectedBalance);
        double actualBalance = user.getBalance();
        
        // Assert
        assertEquals(expectedBalance, actualBalance, 0.001, "getBalance повинен повертати те саме значення, що було встановлено через setBalance");
    }
    
    @Test
    void testSetAndGetCreatedAt() {
        // Arrange
        LocalDateTime expectedCreatedAt = LocalDateTime.now();
        
        // Act
        user.setCreatedAt(expectedCreatedAt);
        LocalDateTime actualCreatedAt = user.getCreatedAt();
        
        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt, "getCreatedAt повинен повертати те саме значення, що було встановлено через setCreatedAt");
    }
    
    @Test
    void testConstructorWithNoArguments() {
        // Act
        User newUser = new User();
        
        // Assert
        assertNotNull(newUser, "Конструктор без аргументів повинен створювати об'єкт");
        assertNull(newUser.getId(), "ID повинен бути null після створення");
    }
    
    @Test
    void testConstructorWithUsernameEmailPassword() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedpassword123";
        
        // Act
        User newUser = new User(username, email, passwordHash);
        
        // Assert
        assertEquals(username, newUser.getUsername(), "getUsername повинен повертати значення, передане в конструктор");
        assertEquals(email, newUser.getEmail(), "getEmail повинен повертати значення, передане в конструктор");
        assertEquals(passwordHash, newUser.getPassword(), "getPassword повинен повертати значення, передане в конструктор");
        assertEquals("USER", newUser.getRole(), "getRole повинен повертати 'USER' за замовчуванням");
        assertEquals(0.0, newUser.getBalance(), 0.001, "getBalance повинен повертати 0.0 за замовчуванням");
        assertNotNull(newUser.getCreatedAt(), "getCreatedAt не повинен повертати null після створення");
    }
    
    @Test
    void testConstructorWithAllArguments() {
        // Arrange
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedpassword123";
        String role = "ADMIN";
        double balance = 1000.50;
        LocalDateTime createdAt = LocalDateTime.now();
        
        // Act
        User newUser = new User(id, username, email, passwordHash, role, balance, createdAt);
        
        // Assert
        assertEquals(id, newUser.getId(), "getId повинен повертати значення, передане в конструктор");
        assertEquals(username, newUser.getUsername(), "getUsername повинен повертати значення, передане в конструктор");
        assertEquals(email, newUser.getEmail(), "getEmail повинен повертати значення, передане в конструктор");
        assertEquals(passwordHash, newUser.getPassword(), "getPassword повинен повертати значення, передане в конструктор");
        assertEquals(role, newUser.getRole(), "getRole повинен повертати значення, передане в конструктор");
        assertEquals(balance, newUser.getBalance(), 0.001, "getBalance повинен повертати значення, передане в конструктор");
        assertEquals(createdAt, newUser.getCreatedAt(), "getCreatedAt повинен повертати значення, передане в конструктор");
    }
    
    @Test
    void testIsAdmin() {
        // Test when role is ADMIN
        user.setRole("ADMIN");
        assertTrue(user.isAdmin(), "isAdmin повинен повертати true, коли роль користувача 'ADMIN'");
        
        // Test when role is not ADMIN
        user.setRole("USER");
        assertFalse(user.isAdmin(), "isAdmin повинен повертати false, коли роль користувача не 'ADMIN'");
    }
}
