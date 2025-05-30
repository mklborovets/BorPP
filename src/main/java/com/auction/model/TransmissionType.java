package com.auction.model;

public enum TransmissionType {
    MANUAL("Механічна"),
    AUTOMATIC("Автоматична"),
    SEMI_AUTOMATIC("Напівавтоматична"),
    ROBOTIC("Роботизована"),
    VARIATOR("Варіатор");
    
    private final String displayName;
    
    TransmissionType(String displayName) {
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