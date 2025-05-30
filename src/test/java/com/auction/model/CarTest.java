package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarTest {

    private Car car;
    
    @BeforeEach
    void setUp() {
        car = new Car();
    }

    @Test
    void testGetType() {
        // Act
        String type = car.getType();
        
        // Assert
        assertEquals(VehicleType.CAR.name(), type, "getType повинен повертати тип транспорту CAR");
    }

    @Test
    void testGetTransmission() {
        // Arrange
        String transmission = "Автоматична";
        car.setTransmission(transmission);
        
        // Act
        String result = car.getTransmission();
        
        // Assert
        assertEquals(transmission, result, "getTransmission повинен повертати стандартне значення трансмісії для автомобілів");
    }
    
    @Test
    void testInheritanceFromVehicle() {
        // Assert
        assertTrue(car instanceof Vehicle, "Car повинен бути підкласом Vehicle");
    }
    
    @Test
    void testSetAndGetBrand() {
        // Arrange
        String expectedBrand = "BMW";
        
        // Act
        car.setBrand(expectedBrand);
        String actualBrand = car.getBrand();
        
        // Assert
        assertEquals(expectedBrand, actualBrand, "getBrand повинен повертати значення, встановлене через setBrand");
    }
    
    @Test
    void testSetAndGetModel() {
        // Arrange
        String expectedModel = "X5";
        
        // Act
        car.setModel(expectedModel);
        String actualModel = car.getModel();
        
        // Assert
        assertEquals(expectedModel, actualModel, "getModel повинен повертати значення, встановлене через setModel");
    }
    
    @Test
    void testSetAndGetYear() {
        // Arrange
        Integer expectedYear = 2021;
        
        // Act
        car.setYear(expectedYear);
        Integer actualYear = car.getYear();
        
        // Assert
        assertEquals(expectedYear, actualYear, "getYear повинен повертати значення, встановлене через setYear");
    }
    
    @Test
    void testSetAndGetCondition() {
        // Arrange
        String expectedCondition = "Новий";
        
        // Act
        car.setCondition(expectedCondition);
        String actualCondition = car.getCondition();
        
        // Assert
        assertEquals(expectedCondition, actualCondition, "getCondition повинен повертати значення, встановлене через setCondition");
    }
    
    @Test
    void testSetAndGetEngine() {
        // Arrange
        String expectedEngine = "V8";
        
        // Act
        car.setEngine(expectedEngine);
        String actualEngine = car.getEngine();
        
        // Assert
        assertEquals(expectedEngine, actualEngine, "getEngine повинен повертати значення, встановлене через setEngine");
    }
    
    @Test
    void testSetAndGetEngineVolume() {
        // Arrange
        double expectedEngineVolume = 3.0;
        
        // Act
        car.setEngineVolume(expectedEngineVolume);
        double actualEngineVolume = car.getEngineVolume();
        
        // Assert
        assertEquals(expectedEngineVolume, actualEngineVolume, 0.001, "getEngineVolume повинен повертати значення, встановлене через setEngineVolume");
    }
}
