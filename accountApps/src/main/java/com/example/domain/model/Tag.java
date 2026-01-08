package com.example.domain.model;

import com.example.domain.valueobject.TypeTag;

public class Tag {
    private String name;
    private String icon;
    private TypeTag type;

    public Tag(String name, String icon, TypeTag type) {
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

    public TypeTag getType() {
        return type;
    }
}
