package com.auction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {
    
    private Vehicle vehicle;
    
    @BeforeEach
    void setUp() {
        // Ініціалізація об'єкта для тестування перед кожним тестом
        vehicle = new Vehicle();
    }
    
    @Test
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 1L;
        
        // Act
        vehicle.setId(expectedId);
        Long actualId = vehicle.getId();
        
        // Assert
        assertEquals(expectedId, actualId, "getId повинен повертати те саме значення, що було встановлено через setId");
    }
    
    @Test
    void testSetAndGetUserId() {
        // Arrange
        Long expectedUserId = 2L;
        
        // Act
        vehicle.setUserId(expectedUserId);
        Long actualUserId = vehicle.getUserId();
        
        // Assert
        assertEquals(expectedUserId, actualUserId, "getUserId повинен повертати те саме значення, що було встановлено через setUserId");
    }
    
    @Test
    void testSetAndGetBrand() {
        // Arrange
        String expectedBrand = "BMW";
        
        // Act
        vehicle.setBrand(expectedBrand);
        String actualBrand = vehicle.getBrand();
        
        // Assert
        assertEquals(expectedBrand, actualBrand, "getBrand повинен повертати те саме значення, що було встановлено через setBrand");
    }
    
    @Test
    void testSetAndGetModel() {
        // Arrange
        String expectedModel = "X5";
        
        // Act
        vehicle.setModel(expectedModel);
        String actualModel = vehicle.getModel();
        
        // Assert
        assertEquals(expectedModel, actualModel, "getModel повинен повертати те саме значення, що було встановлено через setModel");
    }
    
    @Test
    void testSetAndGetYear() {
        // Arrange
        int expectedYear = 2022;
        
        // Act
        vehicle.setYear(expectedYear);
        int actualYear = vehicle.getYear();
        
        // Assert
        assertEquals(expectedYear, actualYear, "getYear повинен повертати те саме значення, що було встановлено через setYear");
    }
    
    @Test
    void testSetAndGetType() {
        // Arrange
        String expectedType = "Седан";
        
        // Act
        vehicle.setType(expectedType);
        String actualType = vehicle.getType();
        
        // Assert
        assertEquals(expectedType, actualType, "getType повинен повертати те саме значення, що було встановлено через setType");
    }
    
    @Test
    void testSetAndGetCondition() {
        // Arrange
        String expectedCondition = "Новий";
        
        // Act
        vehicle.setCondition(expectedCondition);
        String actualCondition = vehicle.getCondition();
        
        // Assert
        assertEquals(expectedCondition, actualCondition, "getCondition повинен повертати те саме значення, що було встановлено через setCondition");
    }
    
    @Test
    void testSetAndGetVin() {
        // Arrange
        String expectedVin = "WBAFG810X0L123456";
        
        // Act
        vehicle.setVin(expectedVin);
        String actualVin = vehicle.getVin();
        
        // Assert
        assertEquals(expectedVin, actualVin, "getVin повинен повертати те саме значення, що було встановлено через setVin");
    }
    
    @Test
    void testSetAndGetMileage() {
        // Arrange
        Integer expectedMileage = 50000;
        
        // Act
        vehicle.setMileage(expectedMileage);
        Integer actualMileage = vehicle.getMileage();
        
        // Assert
        assertEquals(expectedMileage, actualMileage, "getMileage повинен повертати те саме значення, що було встановлено через setMileage");
    }
    
    @Test
    void testSetAndGetEngine() {
        // Arrange
        String expectedEngine = "Бензин";
        
        // Act
        vehicle.setEngine(expectedEngine);
        String actualEngine = vehicle.getEngine();
        
        // Assert
        assertEquals(expectedEngine, actualEngine, "getEngine повинен повертати те саме значення, що було встановлено через setEngine");
    }
    
    @Test
    void testSetAndGetEngineVolume() {
        // Arrange
        double expectedEngineVolume = 3.0;
        
        // Act
        vehicle.setEngineVolume(expectedEngineVolume);
        double actualEngineVolume = vehicle.getEngineVolume();
        
        // Assert
        assertEquals(expectedEngineVolume, actualEngineVolume, 0.001, "getEngineVolume повинен повертати те саме значення, що було встановлено через setEngineVolume");
    }
    
    @Test
    void testSetAndGetPower() {
        // Arrange
        Integer expectedPower = 300;
        
        // Act
        vehicle.setPower(expectedPower);
        Integer actualPower = vehicle.getPower();
        
        // Assert
        assertEquals(expectedPower, actualPower, "getPower повинен повертати те саме значення, що було встановлено через setPower");
    }
    
    @Test
    void testSetAndGetTransmission() {
        // Arrange
        String expectedTransmission = "Автоматична";
        
        // Act
        vehicle.setTransmission(expectedTransmission);
        String actualTransmission = vehicle.getTransmission();
        
        // Assert
        assertEquals(expectedTransmission, actualTransmission, "getTransmission повинен повертати те саме значення, що було встановлено через setTransmission");
    }
    
    @Test
    void testSetAndGetDocuments() {
        // Arrange
        String expectedDocuments = "Повний комплект документів";
        
        // Act
        vehicle.setDocuments(expectedDocuments);
        String actualDocuments = vehicle.getDocuments();
        
        // Assert
        assertEquals(expectedDocuments, actualDocuments, "getDocuments повинен повертати те саме значення, що було встановлено через setDocuments");
    }
    
    @Test
    void testSetAndGetDescription() {
        // Arrange
        String expectedDescription = "Автомобіль у відмінному стані";
        
        // Act
        vehicle.setDescription(expectedDescription);
        String actualDescription = vehicle.getDescription();
        
        // Assert
        assertEquals(expectedDescription, actualDescription, "getDescription повинен повертати те саме значення, що було встановлено через setDescription");
    }
    
    @Test
    void testSetAndGetPhotoUrl() {
        // Arrange
        String expectedPhotoUrl = "photos/bmw_x5_12345.jpg";
        
        // Act
        vehicle.setPhotoUrl(expectedPhotoUrl);
        String actualPhotoUrl = vehicle.getPhotoUrl();
        
        // Assert
        assertEquals(expectedPhotoUrl, actualPhotoUrl, "getPhotoUrl повинен повертати те саме значення, що було встановлено через setPhotoUrl");
    }
    
    @Test
    void testSetAndGetRegistrationDate() {
        // Arrange
        LocalDateTime expectedDate = LocalDateTime.now();
        
        // Act
        vehicle.setRegistrationDate(expectedDate);
        LocalDateTime actualDate = vehicle.getRegistrationDate();
        
        // Assert
        assertEquals(expectedDate, actualDate, "getRegistrationDate повинен повертати те саме значення, що було встановлено через setRegistrationDate");
    }
    
    @Test
    void testSetAndGetCreatedAt() {
        // Arrange
        LocalDateTime expectedDate = LocalDateTime.now();
        
        // Act
        vehicle.setCreatedAt(expectedDate);
        LocalDateTime actualDate = vehicle.getCreatedAt();
        
        // Assert
        assertEquals(expectedDate, actualDate, "getCreatedAt повинен повертати те саме значення, що було встановлено через setCreatedAt");
    }
    
    @Test
    void testConstructorWithNoArguments() {
        // Act
        Vehicle newVehicle = new Vehicle();
        
        // Assert
        assertNotNull(newVehicle, "Конструктор без аргументів повинен створювати об'єкт");
        assertNull(newVehicle.getId(), "ID повинен бути null після створення");
    }
    
    @Test
    void testConstructorWithAllArguments() {
        // Arrange
        Long userId = 2L;
        String brand = "BMW";
        String model = "X5";
        Integer year = 2022;
        String type = "Седан";
        String condition = "Новий";
        String description = "Опис";
        String vin = "WBAFG810X0L123456";
        Integer mileage = 50000;
        String engine = "Бензин";
        double engineVolume = 3.0;
        Integer power = 300;
        String transmission = "Автоматична";
        String documents = "Документи";
        String photoUrl = "photos/bmw_x5_12345.jpg";
        LocalDateTime registrationDate = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();
        
        // Act
        // Використовуємо доступний конструктор з 7 параметрами
        Vehicle newVehicle = new Vehicle(userId, brand, model, year, type, condition, description);
        
        // Встановлюємо решту властивостей через сеттери
        Long id = 1L;
        newVehicle.setId(id);
        newVehicle.setVin(vin);
        newVehicle.setMileage(mileage);
        newVehicle.setEngine(engine);
        newVehicle.setEngineVolume(engineVolume);
        newVehicle.setPower(power);
        newVehicle.setTransmission(transmission);
        newVehicle.setDocuments(documents);
        newVehicle.setPhotoUrl(photoUrl);
        newVehicle.setRegistrationDate(registrationDate);
        newVehicle.setCreatedAt(createdAt);
        
        // Assert
        assertEquals(id, newVehicle.getId(), "getId повинен повертати значення, встановлене через setId");
        assertEquals(userId, newVehicle.getUserId(), "getUserId повинен повертати значення, передане в конструктор");
        assertEquals(brand, newVehicle.getBrand(), "getBrand повинен повертати значення, передане в конструктор");
        assertEquals(model, newVehicle.getModel(), "getModel повинен повертати значення, передане в конструктор");
        assertEquals(year, newVehicle.getYear(), "getYear повинен повертати значення, передане в конструктор");
        assertEquals(type, newVehicle.getType(), "getType повинен повертати значення, передане в конструктор");
        assertEquals(condition, newVehicle.getCondition(), "getCondition повинен повертати значення, передане в конструктор");
        assertEquals(description, newVehicle.getDescription(), "getDescription повинен повертати значення, передане в конструктор");
        assertEquals(vin, newVehicle.getVin(), "getVin повинен повертати значення, встановлене через setVin");
        assertEquals(mileage, newVehicle.getMileage(), "getMileage повинен повертати значення, встановлене через setMileage");
        assertEquals(engine, newVehicle.getEngine(), "getEngine повинен повертати значення, встановлене через setEngine");
        assertEquals(engineVolume, newVehicle.getEngineVolume(), 0.001, "getEngineVolume повинен повертати значення, встановлене через setEngineVolume");
        assertEquals(power, newVehicle.getPower(), "getPower повинен повертати значення, встановлене через setPower");
        assertEquals(transmission, newVehicle.getTransmission(), "getTransmission повинен повертати значення, встановлене через setTransmission");
        assertEquals(documents, newVehicle.getDocuments(), "getDocuments повинен повертати значення, встановлене через setDocuments");
        assertEquals(photoUrl, newVehicle.getPhotoUrl(), "getPhotoUrl повинен повертати значення, встановлене через setPhotoUrl");
        assertEquals(registrationDate, newVehicle.getRegistrationDate(), "getRegistrationDate повинен повертати значення, встановлене через setRegistrationDate");
        assertEquals(createdAt, newVehicle.getCreatedAt(), "getCreatedAt повинен повертати значення, встановлене через setCreatedAt");
    }
}
