package com.example.domain.model;

import java.time.LocalDate;
import java.util.List;
import com.example.domain.valueobject.PaymentMethod;

public class ExpenditureRecord {
    private User user;
    private String name;
    private int money;
    private List<String> category;
    private PaymentMethod payway;
    private LocalDate date;
    
    public ExpenditureRecord(User user, String name, int money, List<String> category, PaymentMethod payway) {
        this(user, name, money, category, payway, LocalDate.now()); // 預設為今日
    }
    
    public ExpenditureRecord(User user, String name, int money, List<String> category, PaymentMethod payway, LocalDate date) {
        this.user = user;
        this.name = name;
        this.money = money;
        this.category = category;
        this.payway = payway;
        this.date = date;
    }
    
    public int getMoney() {
        return money;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getCategory() {
        return category;
    }
    
    public PaymentMethod getPayway() {
        return payway;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
