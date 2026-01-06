package com.example.application.command;

import com.example.domain.model.User;

public class ExpenditureCommand {
    
    private int money;
    private User user;
    private String payway;
    private String category;
    
    public int getMoney() {
        return money;
    }
    
    public void setMoney(int money) {
        this.money = money;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getPayway() {
        return payway;
    }
    
    public void setPayway(String payway) {
        this.payway = payway;
    }
}
