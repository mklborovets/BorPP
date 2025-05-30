package com.auction.model;

import java.time.LocalDateTime;

public class Auction {
    private Long id;
    private Long userId;
    private Long vehicleId;
    private double startPrice;
    private double priceStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
    private String vehicleInfo;
    private double currentPrice;
    private int bidCount;
    private String sellerUsername;

    
    public Auction() {}

    public Auction(Long userId, Long vehicleId, double startPrice, double priceStep,
                  LocalDateTime startTime, LocalDateTime endTime) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.startPrice = startPrice;
        this.priceStep = priceStep;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getPriceStep() {
        return priceStep;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public void setPriceStep(double priceStep) {
        this.priceStep = priceStep;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }
    
    public String getSellerUsername() {
        return sellerUsername;
    }
    
    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }
} 