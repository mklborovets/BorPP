package com.auction.model;

public class VehicleFactory {
    
        public static Vehicle createVehicle(String type) {
        if (type == null) {
            return new Vehicle();
        }
        
        try {
            VehicleType vehicleType = VehicleType.valueOf(type);
            return createVehicle(vehicleType);
        } catch (IllegalArgumentException e) {
            // Якщо тип не знайдено в enum, повертаємо базовий клас
            return new Vehicle();
        }
    }
    
        public static Vehicle createVehicle(VehicleType type) {
        if (type == null) {
            return new Vehicle();
        }
        
        switch (type) {
            case AIRCRAFT:
                return new Aircraft();
            case BOAT:
                return new Boat();
            case CAR:
            case TRUCK:
            case MOTORCYCLE:
            case BUS:
            case VAN:
                return new Car();
            default:
                return new Vehicle();
        }
    }
    
        public static Vehicle createFromVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        
        Vehicle specificVehicle = createVehicle(vehicle.getType());
        
        
        specificVehicle.setId(vehicle.getId());
        specificVehicle.setUserId(vehicle.getUserId());
        specificVehicle.setBrand(vehicle.getBrand());
        specificVehicle.setModel(vehicle.getModel());
        specificVehicle.setYear(vehicle.getYear());
        specificVehicle.setType(vehicle.getType());
        specificVehicle.setCondition(vehicle.getCondition());
        specificVehicle.setDescription(vehicle.getDescription());
        specificVehicle.setVin(vehicle.getVin());
        specificVehicle.setMileage(vehicle.getMileage());
        specificVehicle.setEngineType(vehicle.getEngineType());
        specificVehicle.setPhotoUrl(vehicle.getPhotoUrl());
        specificVehicle.setVideoUrl(vehicle.getVideoUrl());
        specificVehicle.setEngine(vehicle.getEngine());
        specificVehicle.setEngineVolume(vehicle.getEngineVolume());
        specificVehicle.setPower(vehicle.getPower());
        specificVehicle.setTransmission(vehicle.getTransmission());
        specificVehicle.setDocuments(vehicle.getDocuments());
        specificVehicle.setRegistrationDate(vehicle.getRegistrationDate());
        specificVehicle.setCreatedAt(vehicle.getCreatedAt());
        
        return specificVehicle;
    }
}
