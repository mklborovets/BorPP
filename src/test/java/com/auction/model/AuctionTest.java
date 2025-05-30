package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Auction auction;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        auction = new Auction();
    }

    @Test
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 1L;
        
        // Act
        auction.setId(expectedId);
        Long actualId = auction.getId();
        
        // Assert
        assertEquals(expectedId, actualId, "getId повинен повертати значення, встановлене через setId");
    }

    @Test
    void testSetAndGetUserId() {
        // Arrange
        Long expectedUserId = 2L;
        
        // Act
        auction.setUserId(expectedUserId);
        Long actualUserId = auction.getUserId();
        
        // Assert
        assertEquals(expectedUserId, actualUserId, "getUserId повинен повертати значення, встановлене через setUserId");
    }

    @Test
    void testSetAndGetVehicleId() {
        // Arrange
        Long expectedVehicleId = 3L;
        
        // Act
        auction.setVehicleId(expectedVehicleId);
        Long actualVehicleId = auction.getVehicleId();
        
        // Assert
        assertEquals(expectedVehicleId, actualVehicleId, "getVehicleId повинен повертати значення, встановлене через setVehicleId");
    }

    @Test
    void testSetAndGetStartPrice() {
        // Arrange
        double expectedStartPrice = 1000.50;
        
        // Act
        auction.setStartPrice(expectedStartPrice);
        double actualStartPrice = auction.getStartPrice();
        
        // Assert
        assertEquals(expectedStartPrice, actualStartPrice, 0.001, "getStartPrice повинен повертати значення, встановлене через setStartPrice");
    }

    @Test
    void testSetAndGetPriceStep() {
        // Arrange
        double expectedPriceStep = 100.25;
        
        // Act
        auction.setPriceStep(expectedPriceStep);
        double actualPriceStep = auction.getPriceStep();
        
        // Assert
        assertEquals(expectedPriceStep, actualPriceStep, 0.001, "getPriceStep повинен повертати значення, встановлене через setPriceStep");
    }

    @Test
    void testSetAndGetStartTime() {
        // Arrange
        LocalDateTime expectedStartTime = now.plusDays(1);
        
        // Act
        auction.setStartTime(expectedStartTime);
        LocalDateTime actualStartTime = auction.getStartTime();
        
        // Assert
        assertEquals(expectedStartTime, actualStartTime, "getStartTime повинен повертати значення, встановлене через setStartTime");
    }

    @Test
    void testSetAndGetEndTime() {
        // Arrange
        LocalDateTime expectedEndTime = now.plusDays(2);
        
        // Act
        auction.setEndTime(expectedEndTime);
        LocalDateTime actualEndTime = auction.getEndTime();
        
        // Assert
        assertEquals(expectedEndTime, actualEndTime, "getEndTime повинен повертати значення, встановлене через setEndTime");
    }

    @Test
    void testSetAndGetStatus() {
        // Arrange
        String expectedStatus = "ACTIVE";
        
        // Act
        auction.setStatus(expectedStatus);
        String actualStatus = auction.getStatus();
        
        // Assert
        assertEquals(expectedStatus, actualStatus, "getStatus повинен повертати значення, встановлене через setStatus");
    }

    @Test
    void testSetAndGetCreatedAt() {
        // Arrange
        LocalDateTime expectedCreatedAt = now;
        
        // Act
        auction.setCreatedAt(expectedCreatedAt);
        LocalDateTime actualCreatedAt = auction.getCreatedAt();
        
        // Assert
        assertEquals(expectedCreatedAt, actualCreatedAt, "getCreatedAt повинен повертати значення, встановлене через setCreatedAt");
    }

    @Test
    void testSetAndGetVehicleInfo() {
        // Arrange
        String expectedVehicleInfo = "BMW X5 2020";
        
        // Act
        auction.setVehicleInfo(expectedVehicleInfo);
        String actualVehicleInfo = auction.getVehicleInfo();
        
        // Assert
        assertEquals(expectedVehicleInfo, actualVehicleInfo, "getVehicleInfo повинен повертати значення, встановлене через setVehicleInfo");
    }

    @Test
    void testSetAndGetCurrentPrice() {
        // Arrange
        double expectedCurrentPrice = 1200.75;
        
        // Act
        auction.setCurrentPrice(expectedCurrentPrice);
        double actualCurrentPrice = auction.getCurrentPrice();
        
        // Assert
        assertEquals(expectedCurrentPrice, actualCurrentPrice, 0.001, "getCurrentPrice повинен повертати значення, встановлене через setCurrentPrice");
    }

    @Test
    void testSetAndGetBidCount() {
        // Arrange
        int expectedBidCount = 5;
        
        // Act
        auction.setBidCount(expectedBidCount);
        int actualBidCount = auction.getBidCount();
        
        // Assert
        assertEquals(expectedBidCount, actualBidCount, "getBidCount повинен повертати значення, встановлене через setBidCount");
    }

    @Test
    void testSetAndGetSellerUsername() {
        // Arrange
        String expectedSellerUsername = "seller123";
        
        // Act
        auction.setSellerUsername(expectedSellerUsername);
        String actualSellerUsername = auction.getSellerUsername();
        
        // Assert
        assertEquals(expectedSellerUsername, actualSellerUsername, "getSellerUsername повинен повертати значення, встановлене через setSellerUsername");
    }
    
    @Test
    void testConstructorWithNoArguments() {
        // Act
        Auction newAuction = new Auction();
        
        // Assert
        assertNotNull(newAuction, "Конструктор без аргументів повинен створювати об'єкт");
        assertNull(newAuction.getId(), "ID повинен бути null після створення");
    }
    
    @Test
    void testConstructorWithAllArguments() {
        // Arrange
        Long userId = 1L;
        Long vehicleId = 2L;
        double startPrice = 1000.0;
        double priceStep = 100.0;
        LocalDateTime startTime = now.plusDays(1);
        LocalDateTime endTime = now.plusDays(2);
        
        // Act
        Auction newAuction = new Auction(userId, vehicleId, startPrice, priceStep, startTime, endTime);
        
        // Assert
        assertEquals(userId, newAuction.getUserId(), "getUserId повинен повертати значення, передане в конструктор");
        assertEquals(vehicleId, newAuction.getVehicleId(), "getVehicleId повинен повертати значення, передане в конструктор");
        assertEquals(startPrice, newAuction.getStartPrice(), 0.001, "getStartPrice повинен повертати значення, передане в конструктор");
        assertEquals(priceStep, newAuction.getPriceStep(), 0.001, "getPriceStep повинен повертати значення, передане в конструктор");
        assertEquals(startTime, newAuction.getStartTime(), "getStartTime повинен повертати значення, передане в конструктор");
        assertEquals(endTime, newAuction.getEndTime(), "getEndTime повинен повертати значення, передане в конструктор");
        assertEquals("PENDING", newAuction.getStatus(), "getStatus повинен повертати 'PENDING' за замовчуванням");
        assertNotNull(newAuction.getCreatedAt(), "getCreatedAt не повинен повертати null після створення");
    }
}