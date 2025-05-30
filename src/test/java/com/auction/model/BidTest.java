package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidTest {
    
    private Bid bid;
    
    @BeforeEach
    void setUp() {
        // Ініціалізація об'єкта для тестування перед кожним тестом
        bid = new Bid();
    }
    
    @Test
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 1L;
        
        // Act
        bid.setId(expectedId);
        Long actualId = bid.getId();
        
        // Assert
        assertEquals(expectedId, actualId, "getId повинен повертати те саме значення, що було встановлено через setId");
    }
    
    @Test
    void testSetAndGetAuctionId() {
        // Arrange
        Long expectedAuctionId = 2L;
        
        // Act
        bid.setAuctionId(expectedAuctionId);
        Long actualAuctionId = bid.getAuctionId();
        
        // Assert
        assertEquals(expectedAuctionId, actualAuctionId, "getAuctionId повинен повертати те саме значення, що було встановлено через setAuctionId");
    }
    
    @Test
    void testSetAndGetUserId() {
        // Arrange
        Long expectedUserId = 3L;
        
        // Act
        bid.setUserId(expectedUserId);
        Long actualUserId = bid.getUserId();
        
        // Assert
        assertEquals(expectedUserId, actualUserId, "getUserId повинен повертати те саме значення, що було встановлено через setUserId");
    }
    
    @Test
    void testSetAndGetAmount() {
        // Arrange
        double expectedAmount = 1500.75;
        
        // Act
        bid.setAmount(expectedAmount);
        double actualAmount = bid.getAmount();
        
        // Assert
        assertEquals(expectedAmount, actualAmount, 0.001, "getAmount повинен повертати те саме значення, що було встановлено через setAmount");
    }
    
    @Test
    void testSetAndGetCreatedAt() {
        // Arrange
        LocalDateTime expectedCreatedAt = LocalDateTime.now();
        
        // Act
        bid.setCreatedAt(expectedCreatedAt);
        LocalDateTime actualCreatedAt = bid.getCreatedAt();
        
        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt, "getCreatedAt повинен повертати те саме значення, що було встановлено через setCreatedAt");
    }
    
    @Test
    void testSetAndGetUsername() {
        // Arrange
        String expectedUsername = "testuser";
        
        // Act
        bid.setUsername(expectedUsername);
        String actualUsername = bid.getUsername();
        
        // Assert
        assertEquals(expectedUsername, actualUsername, "getUsername повинен повертати те саме значення, що було встановлено через setUsername");
    }
    
    @Test
    void testConstructorWithNoArguments() {
        // Act
        Bid newBid = new Bid();
        
        // Assert
        assertNotNull(newBid, "Конструктор без аргументів повинен створювати об'єкт");
        assertNull(newBid.getId(), "ID повинен бути null після створення");
    }
    
    @Test
    void testConstructorWithAllArguments() {
        // Arrange
        Long id = 1L;
        Long auctionId = 2L;
        Long userId = 3L;
        double amount = 1500.75;
        LocalDateTime createdAt = LocalDateTime.now();
        
        // Act
        Bid newBid = new Bid(id, auctionId, userId, amount, createdAt);
        
        // Assert
        assertEquals(id, newBid.getId(), "getId повинен повертати значення, передане в конструктор");
        assertEquals(auctionId, newBid.getAuctionId(), "getAuctionId повинен повертати значення, передане в конструктор");
        assertEquals(userId, newBid.getUserId(), "getUserId повинен повертати значення, передане в конструктор");
        assertEquals(amount, newBid.getAmount(), 0.001, "getAmount повинен повертати значення, передане в конструктор");
        assertEquals(createdAt, newBid.getCreatedAt(), "getCreatedAt повинен повертати значення, передане в конструктор");
    }
    
    @Test
    void testConstructorWithAuctionIdUserIdAmount() {
        // Arrange
        Long auctionId = 2L;
        Long userId = 3L;
        double amount = 1500.75;
        
        // Act
        Bid newBid = new Bid(auctionId, userId, amount);
        
        // Assert
        assertNull(newBid.getId(), "getId повинен повертати null, оскільки ID не було передано в конструктор");
        assertEquals(auctionId, newBid.getAuctionId(), "getAuctionId повинен повертати значення, передане в конструктор");
        assertEquals(userId, newBid.getUserId(), "getUserId повинен повертати значення, передане в конструктор");
        assertEquals(amount, newBid.getAmount(), 0.001, "getAmount повинен повертати значення, передане в конструктор");
        assertNotNull(newBid.getCreatedAt(), "getCreatedAt не повинен повертати null після створення");
    }
}
