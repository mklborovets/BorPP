package com.auction.model;

import java.time.LocalDateTime;

public class Log {
    private Long id;
    private Long userId;
    private String action;
    private LocalDateTime createdAt;

    
    public Log() {}

    public Log(Long userId, String action) {
        this.userId = userId;
        this.action = action;
        this.createdAt = LocalDateTime.now();
    }
    
    public Log(Long id, Long userId, String action, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 