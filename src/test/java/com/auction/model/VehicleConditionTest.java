package com.auction.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class VehicleConditionTest {

    @Test
    void testEnumValues() {
        // Перевірка кількості значень у енумі
        assertEquals(4, VehicleCondition.values().length, "Енум VehicleCondition повинен мати 4 значення");
        
        // Перевірка наявності всіх очікуваних значень
        assertNotNull(VehicleCondition.NEW);
        assertNotNull(VehicleCondition.USED);
        assertNotNull(VehicleCondition.DAMAGED);
        assertNotNull(VehicleCondition.FOR_PARTS);
    }
    
    @ParameterizedTest
    @EnumSource(VehicleCondition.class)
    void testGetDisplayName(VehicleCondition condition) {
        // Перевірка, що displayName не null і не порожній
        assertNotNull(condition.getDisplayName(), "DisplayName не повинен бути null");
        assertFalse(condition.getDisplayName().isEmpty(), "DisplayName не повинен бути порожнім");
    }
    
    @Test
    void testSpecificDisplayNames() {
        // Перевірка конкретних значень displayName
        assertEquals("Новий", VehicleCondition.NEW.getDisplayName());
        assertEquals("Вживаний", VehicleCondition.USED.getDisplayName());
        assertEquals("Пошкоджений", VehicleCondition.DAMAGED.getDisplayName());
        assertEquals("На запчастини", VehicleCondition.FOR_PARTS.getDisplayName());
    }
    
    @ParameterizedTest
    @EnumSource(VehicleCondition.class)
    void testToString(VehicleCondition condition) {
        // Перевірка, що toString повертає те саме, що й getDisplayName
        assertEquals(condition.getDisplayName(), condition.toString(), "toString повинен повертати те саме, що й getDisplayName");
    }
    
    @Test
    void testConditionUsageInVehicle() {
        // Перевірка використання умов у транспортних засобах
        Vehicle car = new Car();
        
        // Перевірка встановлення та отримання умови
        car.setCondition(VehicleCondition.NEW.name());
        assertEquals(VehicleCondition.NEW.name(), car.getCondition());
        
        car.setCondition(VehicleCondition.USED.name());
        assertEquals(VehicleCondition.USED.name(), car.getCondition());
        
        car.setCondition(VehicleCondition.DAMAGED.name());
        assertEquals(VehicleCondition.DAMAGED.name(), car.getCondition());
        
        car.setCondition(VehicleCondition.FOR_PARTS.name());
        assertEquals(VehicleCondition.FOR_PARTS.name(), car.getCondition());
    }
}
