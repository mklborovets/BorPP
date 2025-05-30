package com.auction.model;

public enum VehicleCondition {
    NEW("Новий"),
    USED("Вживаний"),
    DAMAGED("Пошкоджений"),
    FOR_PARTS("На запчастини");
    
    private final String displayName;
    
    VehicleCondition(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 