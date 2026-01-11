package com.example.application.command;

import java.util.List;
import com.example.domain.model.User;

public class ExpenditureCommand {
    
    private String name;
    private int money;
    private User user;
    private String payway;
    private List<String> category;
    
    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

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
    
    public void setCategory(List<String> category) {
        this.category = category;
    }
    
    public List<String> getCategory() {
        return category;
    }
    
    public String getPayway() {
        return payway;
    }
    
    public void setPayway(String payway) {
        this.payway = payway;
    }
}
