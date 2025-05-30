package com.auction.model;

public enum VehicleType {
    CAR("Легковий автомобіль"),
    TRUCK("Вантажний автомобіль"),
    MOTORCYCLE("Мотоцикл"),
    BUS("Автобус"),
    VAN("Мікроавтобус"),
    AIRCRAFT("Літак"),
    BOAT("Човен");
    
    private final String displayName;
    
    VehicleType(String displayName) {
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