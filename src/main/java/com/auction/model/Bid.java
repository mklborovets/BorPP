package com.auction.model;

import java.time.LocalDateTime;

public class Bid {
    private Long id;
    private Long userId;
    private Long auctionId;
    private double amount;
    private LocalDateTime createdAt;
    private String username;

    
    public Bid() {
    }

    public Bid(Long userId, Long auctionId, double amount) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
    
    public Bid(Long id, Long auctionId, Long userId, double amount, LocalDateTime createdAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 