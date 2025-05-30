package com.auction.model;

public class Car extends Vehicle {

    public Car() {
        super();
    }
    
        @Override
    public String getType() {
        return VehicleType.CAR.name();
    }
    
        @Override
    public String getTransmission() {
        return super.getTransmission(); 
    }
}
