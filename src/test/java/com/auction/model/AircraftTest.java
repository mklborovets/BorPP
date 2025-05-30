package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    private Aircraft aircraft;
    
    @BeforeEach
    void setUp() {
        aircraft = new Aircraft();
    }

    @Test
    void testGetType() {
        // Act
        String type = aircraft.getType();
        
        // Assert
        assertEquals(VehicleType.AIRCRAFT.name(), type, "getType повинен повертати тип транспорту AIRCRAFT");
    }

    @Test
    void testGetTransmission() {
        // Arrange
        String transmission = "Автоматична";
        aircraft.setTransmission(transmission);
        
        // Act
        String result = aircraft.getTransmission();
        
        // Assert
        assertEquals("Тип управління: " + transmission, result, "getTransmission повинен повертати тип управління з префіксом");
    }

    @Test
    void testGetEngine() {
        // Arrange
        String engine = "Турбореактивний";
        aircraft.setEngine(engine);
        
        // Act
        String result = aircraft.getEngine();
        
        // Assert
        assertEquals("Авіаційний двигун: " + engine, result, "getEngine повинен повертати тип двигуна з префіксом");
    }
    
    @Test
    void testInheritanceFromVehicle() {
        // Assert
        assertTrue(aircraft instanceof Vehicle, "Aircraft повинен бути підкласом Vehicle");
    }
    
    @Test
    void testSetAndGetBrand() {
        // Arrange
        String expectedBrand = "Boeing";
        
        // Act
        aircraft.setBrand(expectedBrand);
        String actualBrand = aircraft.getBrand();
        
        // Assert
        assertEquals(expectedBrand, actualBrand, "getBrand повинен повертати значення, встановлене через setBrand");
    }
    
    @Test
    void testSetAndGetModel() {
        // Arrange
        String expectedModel = "747";
        
        // Act
        aircraft.setModel(expectedModel);
        String actualModel = aircraft.getModel();
        
        // Assert
        assertEquals(expectedModel, actualModel, "getModel повинен повертати значення, встановлене через setModel");
    }
    
    @Test
    void testSetAndGetYear() {
        // Arrange
        Integer expectedYear = 2020;
        
        // Act
        aircraft.setYear(expectedYear);
        Integer actualYear = aircraft.getYear();
        
        // Assert
        assertEquals(expectedYear, actualYear, "getYear повинен повертати значення, встановлене через setYear");
    }
    
    @Test
    void testSetAndGetCondition() {
        // Arrange
        String expectedCondition = "Новий";
        
        // Act
        aircraft.setCondition(expectedCondition);
        String actualCondition = aircraft.getCondition();
        
        // Assert
        assertEquals(expectedCondition, actualCondition, "getCondition повинен повертати значення, встановлене через setCondition");
    }
    
    @Test
    void testSetAndGetDescription() {
        // Arrange
        String expectedDescription = "Пасажирський літак у відмінному стані";
        
        // Act
        aircraft.setDescription(expectedDescription);
        String actualDescription = aircraft.getDescription();
        
        // Assert
        assertEquals(expectedDescription, actualDescription, "getDescription повинен повертати значення, встановлене через setDescription");
    }
}