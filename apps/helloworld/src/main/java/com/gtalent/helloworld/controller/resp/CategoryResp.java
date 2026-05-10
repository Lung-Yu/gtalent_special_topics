package com.gtalent.helloworld.controller.resp;

import java.time.LocalDateTime;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.valueobject.TypeCategory;

public class CategoryResp {

    private Long id;
    private String name;
    private String icon;
    private TypeCategory type;
    private LocalDateTime createdAt;
    private String createdByUsername;

    public static CategoryResp from(Category category) {
        CategoryResp resp = new CategoryResp();
        resp.id = category.getId();
        resp.name = category.getName();
        resp.icon = category.getIcon();
        resp.type = category.getType();
        resp.createdAt = category.getCreatedAt();
        resp.createdByUsername = category.getCreatedBy() != null
                ? category.getCreatedBy().getUsername() : null;
        return resp;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public TypeCategory getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
}
