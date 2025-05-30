package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoatTest {

    private Boat boat;
    
    @BeforeEach
    void setUp() {
        boat = new Boat();
    }

    @Test
    void testGetType() {
        // Act
        String type = boat.getType();
        
        // Assert
        assertEquals(VehicleType.BOAT.name(), type, "getType повинен повертати тип транспорту BOAT");
    }

    @Test
    void testGetTransmission() {
        // Arrange
        String transmission = "Гідравлічний";
        boat.setTransmission(transmission);
        
        // Act
        String result = boat.getTransmission();
        
        // Assert
        assertEquals("Тип приводу: " + transmission, result, "getTransmission повинен повертати тип приводу з префіксом");
    }

    @Test
    void testGetEngine() {
        // Arrange
        String engine = "Підвісний";
        boat.setEngine(engine);
        
        // Act
        String result = boat.getEngine();
        
        // Assert
        assertEquals("Судновий двигун: " + engine, result, "getEngine повинен повертати тип двигуна з префіксом");
    }
    
    @Test
    void testInheritanceFromVehicle() {
        // Assert
        assertTrue(boat instanceof Vehicle, "Boat повинен бути підкласом Vehicle");
    }
    
    @Test
    void testSetAndGetBrand() {
        // Arrange
        String expectedBrand = "Bayliner";
        
        // Act
        boat.setBrand(expectedBrand);
        String actualBrand = boat.getBrand();
        
        // Assert
        assertEquals(expectedBrand, actualBrand, "getBrand повинен повертати значення, встановлене через setBrand");
    }
    
    @Test
    void testSetAndGetModel() {
        // Arrange
        String expectedModel = "Element E16";
        
        // Act
        boat.setModel(expectedModel);
        String actualModel = boat.getModel();
        
        // Assert
        assertEquals(expectedModel, actualModel, "getModel повинен повертати значення, встановлене через setModel");
    }
    
    @Test
    void testSetAndGetYear() {
        // Arrange
        Integer expectedYear = 2022;
        
        // Act
        boat.setYear(expectedYear);
        Integer actualYear = boat.getYear();
        
        // Assert
        assertEquals(expectedYear, actualYear, "getYear повинен повертати значення, встановлене через setYear");
    }
    
    @Test
    void testSetAndGetCondition() {
        // Arrange
        String expectedCondition = "Відмінний";
        
        // Act
        boat.setCondition(expectedCondition);
        String actualCondition = boat.getCondition();
        
        // Assert
        assertEquals(expectedCondition, actualCondition, "getCondition повинен повертати значення, встановлене через setCondition");
    }
    
    @Test
    void testSetAndGetDescription() {
        // Arrange
        String expectedDescription = "Моторний човен для відпочинку на воді";
        
        // Act
        boat.setDescription(expectedDescription);
        String actualDescription = boat.getDescription();
        
        // Assert
        assertEquals(expectedDescription, actualDescription, "getDescription повинен повертати значення, встановлене через setDescription");
    }
}
