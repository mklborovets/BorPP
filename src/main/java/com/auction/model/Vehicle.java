package com.auction.model;

import java.time.LocalDateTime;

public class Vehicle {
    private Long id;
    private Long userId;
    private String brand;
    private String model;
    private Integer year;
    private String type;
    private String condition;
    private String description;
    private String vin;
    private Integer mileage;
    private String engineType;
    private String photoUrl;
    private String videoUrl;
    private String engine;
    private double engineVolume;
    private Integer power;
    private String transmission;
    private String documents;
    private LocalDateTime registrationDate;
    private LocalDateTime createdAt;

    
    public Vehicle() {}

    public Vehicle(Long userId, String brand, String model, Integer year, 
                  String type, String condition, String description) {
        this.userId = userId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.type = type;
        this.condition = condition;
        this.description = description;
    }
    
    public Vehicle(Long id, Long userId, String brand, String model, Integer year, 
                  String type, String condition, String description, String vin, 
                  Integer mileage, String engine, double engineVolume, Integer power, 
                  String transmission, String documents, String photoUrl, String videoUrl, 
                  LocalDateTime registrationDate, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.type = type;
        this.condition = condition;
        this.description = description;
        this.vin = vin;
        this.mileage = mileage;
        this.engine = engine;
        this.engineVolume = engineVolume;
        this.power = power;
        this.transmission = transmission;
        this.documents = documents;
        this.photoUrl = photoUrl;
        this.videoUrl = videoUrl;
        this.registrationDate = registrationDate;
        this.createdAt = createdAt;
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public double getEngineVolume() {
        return engineVolume;
    }

    public void setEngineVolume(double engineVolume) {
        this.engineVolume = engineVolume;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getDocuments() {
        return documents;
    }

    public void setDocuments(String documents) {
        this.documents = documents;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 