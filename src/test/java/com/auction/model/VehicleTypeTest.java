package com.auction.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTypeTest {

    @Test
    void testEnumValues() {
        // Перевірка кількості значень у енумі
        assertEquals(7, VehicleType.values().length, "Енум VehicleType повинен мати 7 значень");
        
        // Перевірка наявності всіх очікуваних значень
        assertNotNull(VehicleType.CAR);
        assertNotNull(VehicleType.TRUCK);
        assertNotNull(VehicleType.MOTORCYCLE);
        assertNotNull(VehicleType.BUS);
        assertNotNull(VehicleType.VAN);
        assertNotNull(VehicleType.AIRCRAFT);
        assertNotNull(VehicleType.BOAT);
    }
    
    @ParameterizedTest
    @EnumSource(VehicleType.class)
    void testGetDisplayName(VehicleType type) {
        // Перевірка, що displayName не null і не порожній
        assertNotNull(type.getDisplayName(), "DisplayName не повинен бути null");
        assertFalse(type.getDisplayName().isEmpty(), "DisplayName не повинен бути порожнім");
    }
    
    @Test
    void testSpecificDisplayNames() {
        // Перевірка конкретних значень displayName
        assertEquals("Легковий автомобіль", VehicleType.CAR.getDisplayName());
        assertEquals("Вантажний автомобіль", VehicleType.TRUCK.getDisplayName());
        assertEquals("Мотоцикл", VehicleType.MOTORCYCLE.getDisplayName());
        assertEquals("Автобус", VehicleType.BUS.getDisplayName());
        assertEquals("Мікроавтобус", VehicleType.VAN.getDisplayName());
        assertEquals("Літак", VehicleType.AIRCRAFT.getDisplayName());
        assertEquals("Човен", VehicleType.BOAT.getDisplayName());
    }
    
    @ParameterizedTest
    @EnumSource(VehicleType.class)
    void testToString(VehicleType type) {
        // Перевірка, що toString повертає те саме, що й getDisplayName
        assertEquals(type.getDisplayName(), type.toString(), "toString повинен повертати те саме, що й getDisplayName");
    }
    
    @Test
    void testCarLikeTypes() {
        // Перевірка, що CAR, TRUCK, MOTORCYCLE, BUS, VAN - це типи, які можуть бути представлені як Car
        VehicleType[] carLikeTypes = {VehicleType.CAR, VehicleType.TRUCK, VehicleType.MOTORCYCLE, VehicleType.BUS, VehicleType.VAN};
        
        for (VehicleType type : carLikeTypes) {
            Vehicle vehicle = VehicleFactory.createVehicle(type);
            assertTrue(vehicle instanceof Car, "Тип " + type + " повинен створювати екземпляр Car");
        }
    }
    
    @Test
    void testNonCarTypes() {
        // Перевірка, що AIRCRAFT і BOAT не є типами Car
        Vehicle aircraft = VehicleFactory.createVehicle(VehicleType.AIRCRAFT);
        Vehicle boat = VehicleFactory.createVehicle(VehicleType.BOAT);
        
        assertTrue(aircraft instanceof Aircraft, "AIRCRAFT повинен створювати екземпляр Aircraft");
        assertTrue(boat instanceof Boat, "BOAT повинен створювати екземпляр Boat");
        
        assertFalse(aircraft instanceof Car, "AIRCRAFT не повинен створювати екземпляр Car");
        assertFalse(boat instanceof Car, "BOAT не повинен створювати екземпляр Car");
    }
}
