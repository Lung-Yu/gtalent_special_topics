package com.example.domain.model;

import com.example.domain.valueobject.TypeCategory;

public class Category {
    private String name;
    private String icon;
    private TypeCategory type;

    public Category(String name, String icon, TypeCategory type) {
        this.name = name;
        this.icon = icon;
        this.type = type;
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
}
