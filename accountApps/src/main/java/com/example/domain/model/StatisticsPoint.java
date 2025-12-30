package com.example.domain.model;

import java.time.LocalDateTime;

import com.example.domain.valueobject.StatisticsCategory;

public class StatisticsPoint {
    private int amount;
    private User user;
    private LocalDateTime time;
    private StatisticsCategory category;
    
    public StatisticsPoint() {
    }
    
    public StatisticsPoint(int amount, User user, LocalDateTime time, StatisticsCategory category) {
        this.amount = amount;
        this.user = user;
        this.time = time;
        this.category = category;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getTime() {
        return time;
    }
    
    public void setTime(LocalDateTime time) {
        this.time = time;
    }
    
    public StatisticsCategory getCategory() {
        return category;
    }
    
    public void setCategory(StatisticsCategory category) {
        this.category = category;
    }
}
