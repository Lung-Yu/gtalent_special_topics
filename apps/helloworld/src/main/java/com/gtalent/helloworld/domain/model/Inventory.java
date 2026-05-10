package com.gtalent.helloworld.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "inventories")
public class Inventory {

    /** 格式：2 個英文字母 + 6 個數字，例如 AF000001 */
    @Id
    @Pattern(regexp = "[a-zA-Z]{2}\\d{6}", message = "ID 格式須為 2 個英文字母加 6 位數字，例如 AF000001")
    @Column(nullable = false)
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    protected Inventory() {}

    public Inventory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
