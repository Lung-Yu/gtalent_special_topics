package com.example.application.command;

import java.time.LocalDate;

import com.example.domain.model.User;

public class ConsumptionCommand {
    private int money;
    private User user;
    private String name;
    private String category;
    private LocalDate date;

    public ConsumptionCommand(String name, String category, int money, User user, LocalDate date) {
        setName(name);
        setCategory(category);
        setMoney(money);
        setUser(user);
        setDate(date);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if(money < 0){
            throw new IllegalArgumentException("Money cannot be negative");
        }
        
        this.money = money;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
