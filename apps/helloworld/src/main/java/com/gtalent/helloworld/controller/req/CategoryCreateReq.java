package com.gtalent.helloworld.controller.req;

import com.gtalent.helloworld.domain.valueobject.TypeCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryCreateReq {

    @NotBlank
    private String name;

    private String icon;

    @NotNull
    private TypeCategory type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public TypeCategory getType() { return type; }
    public void setType(TypeCategory type) { this.type = type; }
}
