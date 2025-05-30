package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LogTest {
    
    private Log log;
    
    @BeforeEach
    void setUp() {
        // Ініціалізація об'єкта для тестування перед кожним тестом
        log = new Log();
    }
    
    @Test
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 1L;
        
        // Act
        log.setId(expectedId);
        Long actualId = log.getId();
        
        // Assert
        assertEquals(expectedId, actualId, "getId повинен повертати те саме значення, що було встановлено через setId");
    }
    
    @Test
    void testSetAndGetUserId() {
        // Arrange
        Long expectedUserId = 2L;
        
        // Act
        log.setUserId(expectedUserId);
        Long actualUserId = log.getUserId();
        
        // Assert
        assertEquals(expectedUserId, actualUserId, "getUserId повинен повертати те саме значення, що було встановлено через setUserId");
    }
    
    @Test
    void testSetAndGetAction() {
        // Arrange
        String expectedAction = "Створено аукціон";
        
        // Act
        log.setAction(expectedAction);
        String actualAction = log.getAction();
        
        // Assert
        assertEquals(expectedAction, actualAction, "getAction повинен повертати те саме значення, що було встановлено через setAction");
    }
    
    @Test
    void testSetAndGetTimestamp() {
        // Arrange
        LocalDateTime expectedTimestamp = LocalDateTime.now();
        
        // Act
        log.setCreatedAt(expectedTimestamp);
        LocalDateTime actualTimestamp = log.getCreatedAt();
        
        // Assert
        assertEquals(expectedTimestamp, actualTimestamp, "getCreatedAt повинен повертати те саме значення, що було встановлено через setCreatedAt");
    }
    
    @Test
    void testConstructorWithNoArguments() {
        // Act
        Log newLog = new Log();
        
        // Assert
        assertNotNull(newLog, "Конструктор без аргументів повинен створювати об'єкт");
        assertNull(newLog.getId(), "ID повинен бути null після створення");
    }
    
    @Test
    void testConstructorWithUserIdAction() {
        // Arrange
        Long userId = 2L;
        String action = "Створено аукціон";
        
        // Act
        Log newLog = new Log(userId, action);
        
        // Assert
        assertNull(newLog.getId(), "getId повинен повертати null, оскільки ID не було передано в конструктор");
        assertEquals(userId, newLog.getUserId(), "getUserId повинен повертати значення, передане в конструктор");
        assertEquals(action, newLog.getAction(), "getAction повинен повертати значення, передане в конструктор");
        assertNotNull(newLog.getCreatedAt(), "getCreatedAt не повинен повертати null після створення");
    }
    
    @Test
    void testConstructorWithAllArguments() {
        // Arrange
        Long id = 1L;
        Long userId = 2L;
        String action = "Створено аукціон";
        LocalDateTime createdAt = LocalDateTime.now();
        
        // Act
        Log newLog = new Log(id, userId, action, createdAt);
        
        // Assert
        assertEquals(id, newLog.getId(), "getId повинен повертати значення, передане в конструктор");
        assertEquals(userId, newLog.getUserId(), "getUserId повинен повертати значення, передане в конструктор");
        assertEquals(action, newLog.getAction(), "getAction повинен повертати значення, передане в конструктор");
        assertEquals(createdAt, newLog.getCreatedAt(), "getCreatedAt повинен повертати значення, передане в конструктор");
    }
}
