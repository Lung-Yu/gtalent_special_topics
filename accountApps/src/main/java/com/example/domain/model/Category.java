package com.example.domain.model;

import java.time.LocalDateTime;
import com.example.domain.valueobject.TypeCategory;

public class Category {
    private String name;
    private String icon;
    private TypeCategory type;
    private LocalDateTime createdAt;
    private User createdBy;

    public Category(String name, String icon, TypeCategory type, User createdBy) {
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public TypeCategory getType() {
        return type;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
}
