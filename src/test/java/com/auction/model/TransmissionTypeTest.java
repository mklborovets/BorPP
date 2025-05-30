package com.auction.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class TransmissionTypeTest {

    @Test
    void testEnumValues() {
        // Перевірка кількості значень у енумі
        assertEquals(5, TransmissionType.values().length, "Енум TransmissionType повинен мати 5 значень");
        
        // Перевірка наявності всіх очікуваних значень
        assertNotNull(TransmissionType.MANUAL);
        assertNotNull(TransmissionType.AUTOMATIC);
        assertNotNull(TransmissionType.SEMI_AUTOMATIC);
        assertNotNull(TransmissionType.ROBOTIC);
        assertNotNull(TransmissionType.VARIATOR);
    }
    
    @ParameterizedTest
    @EnumSource(TransmissionType.class)
    void testGetDisplayName(TransmissionType type) {
        // Перевірка, що displayName не null і не порожній
        assertNotNull(type.getDisplayName(), "DisplayName не повинен бути null");
        assertFalse(type.getDisplayName().isEmpty(), "DisplayName не повинен бути порожнім");
    }
    
    @Test
    void testSpecificDisplayNames() {
        // Перевірка конкретних значень displayName
        assertEquals("Механічна", TransmissionType.MANUAL.getDisplayName());
        assertEquals("Автоматична", TransmissionType.AUTOMATIC.getDisplayName());
        assertEquals("Напівавтоматична", TransmissionType.SEMI_AUTOMATIC.getDisplayName());
        assertEquals("Роботизована", TransmissionType.ROBOTIC.getDisplayName());
        assertEquals("Варіатор", TransmissionType.VARIATOR.getDisplayName());
    }
    
    @ParameterizedTest
    @EnumSource(TransmissionType.class)
    void testToString(TransmissionType type) {
        // Перевірка, що toString повертає те саме, що й getDisplayName
        assertEquals(type.getDisplayName(), type.toString(), "toString повинен повертати те саме, що й getDisplayName");
    }
}
