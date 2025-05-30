package com.auction.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VehicleFactoryTest {

    @Test
    void testCreateVehicleWithNullStringType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle((String) null);
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Vehicle навіть з null типом");
        assertTrue(vehicle instanceof Vehicle, "Повернутий об'єкт повинен бути екземпляром Vehicle");
        assertFalse(vehicle instanceof Car, "Повернутий об'єкт не повинен бути екземпляром Car");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"INVALID_TYPE", "NOT_A_VEHICLE", "RANDOM"})
    void testCreateVehicleWithInvalidStringType(String invalidType) {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle(invalidType);
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Vehicle навіть з невалідним типом");
        assertTrue(vehicle instanceof Vehicle, "Повернутий об'єкт повинен бути екземпляром Vehicle");
        assertFalse(vehicle instanceof Car, "Повернутий об'єкт не повинен бути екземпляром Car");
    }
    
    @Test
    void testCreateVehicleWithCarStringType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle("CAR");
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Car для типу CAR");
        assertTrue(vehicle instanceof Car, "Повернутий об'єкт повинен бути екземпляром Car");
        assertEquals(VehicleType.CAR.name(), vehicle.getType(), "Тип транспорту повинен бути CAR");
    }
    
    @Test
    void testCreateVehicleWithBoatStringType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle("BOAT");
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Boat для типу BOAT");
        assertTrue(vehicle instanceof Boat, "Повернутий об'єкт повинен бути екземпляром Boat");
        assertEquals(VehicleType.BOAT.name(), vehicle.getType(), "Тип транспорту повинен бути BOAT");
    }
    
    @Test
    void testCreateVehicleWithAircraftStringType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle("AIRCRAFT");
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Aircraft для типу AIRCRAFT");
        assertTrue(vehicle instanceof Aircraft, "Повернутий об'єкт повинен бути екземпляром Aircraft");
        assertEquals(VehicleType.AIRCRAFT.name(), vehicle.getType(), "Тип транспорту повинен бути AIRCRAFT");
    }
    
    @Test
    void testCreateVehicleWithNullEnumType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle((VehicleType) null);
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Vehicle навіть з null типом");
        assertTrue(vehicle instanceof Vehicle, "Повернутий об'єкт повинен бути екземпляром Vehicle");
        assertFalse(vehicle instanceof Car, "Повернутий об'єкт не повинен бути екземпляром Car");
    }
    
    @ParameterizedTest
    @EnumSource(value = VehicleType.class, names = {"CAR", "TRUCK", "MOTORCYCLE", "BUS", "VAN"})
    void testCreateVehicleWithCarLikeEnumTypes(VehicleType type) {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle(type);
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Car для типів CAR, TRUCK, MOTORCYCLE, BUS, VAN");
        assertTrue(vehicle instanceof Car, "Повернутий об'єкт повинен бути екземпляром Car");
    }
    
    @Test
    void testCreateVehicleWithBoatEnumType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle(VehicleType.BOAT);
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Boat для типу BOAT");
        assertTrue(vehicle instanceof Boat, "Повернутий об'єкт повинен бути екземпляром Boat");
    }
    
    @Test
    void testCreateVehicleWithAircraftEnumType() {
        // Act
        Vehicle vehicle = VehicleFactory.createVehicle(VehicleType.AIRCRAFT);
        
        // Assert
        assertNotNull(vehicle, "Фабрика повинна повертати об'єкт Aircraft для типу AIRCRAFT");
        assertTrue(vehicle instanceof Aircraft, "Повернутий об'єкт повинен бути екземпляром Aircraft");
    }
    
    @Test
    void testCreateFromVehicleWithNull() {
        // Act
        Vehicle result = VehicleFactory.createFromVehicle(null);
        
        // Assert
        assertNull(result, "Фабрика повинна повертати null при передачі null");
    }
    
    @Test
    void testCreateFromVehicleWithCar() {
        // Arrange
        Vehicle originalVehicle = new Vehicle();
        originalVehicle.setType(VehicleType.CAR.name());
        originalVehicle.setId(1L);
        originalVehicle.setUserId(2L);
        originalVehicle.setBrand("Toyota");
        originalVehicle.setModel("Camry");
        originalVehicle.setYear(2020);
        originalVehicle.setCondition("Новий");
        originalVehicle.setDescription("Опис автомобіля");
        originalVehicle.setVin("ABC123456789");
        originalVehicle.setMileage(5000);
        originalVehicle.setEngine("V6");
        originalVehicle.setEngineVolume(2.5);
        originalVehicle.setPower(200);
        originalVehicle.setTransmission("Автоматична");
        LocalDateTime now = LocalDateTime.now();
        originalVehicle.setCreatedAt(now);
        
        // Act
        Vehicle result = VehicleFactory.createFromVehicle(originalVehicle);
        
        // Assert
        assertNotNull(result, "Результат не повинен бути null");
        assertTrue(result instanceof Car, "Результат повинен бути екземпляром Car");
        assertEquals(originalVehicle.getId(), result.getId(), "ID повинен бути скопійований");
        assertEquals(originalVehicle.getUserId(), result.getUserId(), "UserID повинен бути скопійований");
        assertEquals(originalVehicle.getBrand(), result.getBrand(), "Brand повинен бути скопійований");
        assertEquals(originalVehicle.getModel(), result.getModel(), "Model повинен бути скопійований");
        assertEquals(originalVehicle.getYear(), result.getYear(), "Year повинен бути скопійований");
        assertEquals(originalVehicle.getCondition(), result.getCondition(), "Condition повинен бути скопійований");
        assertEquals(originalVehicle.getDescription(), result.getDescription(), "Description повинен бути скопійований");
        assertEquals(originalVehicle.getVin(), result.getVin(), "VIN повинен бути скопійований");
        assertEquals(originalVehicle.getMileage(), result.getMileage(), "Mileage повинен бути скопійований");
        assertEquals(originalVehicle.getEngine(), result.getEngine(), "Engine повинен бути скопійований");
        assertEquals(originalVehicle.getEngineVolume(), result.getEngineVolume(), 0.001, "EngineVolume повинен бути скопійований");
        assertEquals(originalVehicle.getPower(), result.getPower(), "Power повинен бути скопійований");
        assertEquals(originalVehicle.getTransmission(), result.getTransmission(), "Transmission повинен бути скопійований");
        assertEquals(originalVehicle.getCreatedAt(), result.getCreatedAt(), "CreatedAt повинен бути скопійований");
    }
    
    @Test
    void testCreateFromVehicleWithBoat() {
        // Arrange
        Vehicle originalVehicle = new Vehicle();
        originalVehicle.setType(VehicleType.BOAT.name());
        originalVehicle.setBrand("Bayliner");
        originalVehicle.setModel("Element");
        
        // Act
        Vehicle result = VehicleFactory.createFromVehicle(originalVehicle);
        
        // Assert
        assertNotNull(result, "Результат не повинен бути null");
        assertTrue(result instanceof Boat, "Результат повинен бути екземпляром Boat");
        assertEquals(originalVehicle.getBrand(), result.getBrand(), "Brand повинен бути скопійований");
        assertEquals(originalVehicle.getModel(), result.getModel(), "Model повинен бути скопійований");
    }
    
    @Test
    void testCreateFromVehicleWithAircraft() {
        // Arrange
        Vehicle originalVehicle = new Vehicle();
        originalVehicle.setType(VehicleType.AIRCRAFT.name());
        originalVehicle.setBrand("Boeing");
        originalVehicle.setModel("747");
        
        // Act
        Vehicle result = VehicleFactory.createFromVehicle(originalVehicle);
        
        // Assert
        assertNotNull(result, "Результат не повинен бути null");
        assertTrue(result instanceof Aircraft, "Результат повинен бути екземпляром Aircraft");
        assertEquals(originalVehicle.getBrand(), result.getBrand(), "Brand повинен бути скопійований");
        assertEquals(originalVehicle.getModel(), result.getModel(), "Model повинен бути скопійований");
    }
}
