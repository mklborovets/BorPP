package com.auction.model;

public class Aircraft extends Vehicle {
    
    public Aircraft() {
        super();
    }
    
        @Override
    public String getType() {
        return VehicleType.AIRCRAFT.name();
    }
    
        @Override
    public String getTransmission() {
        return "Тип управління: " + super.getTransmission();
    }
    
        @Override
    public String getEngine() {
        return "Авіаційний двигун: " + super.getEngine();
    }
}
