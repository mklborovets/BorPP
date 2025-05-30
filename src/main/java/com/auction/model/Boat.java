package com.auction.model;

public class Boat extends Vehicle {
    
    public Boat() {
        super();
    }
    
        @Override
    public String getType() {
        return VehicleType.BOAT.name();
    }
    
        @Override
    public String getTransmission() {
        return "Тип приводу: " + super.getTransmission();
    }
    
        @Override
    public String getEngine() {
        return "Судновий двигун: " + super.getEngine();
    }
}
