package com.gtalent.helloworld.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.gtalent.helloworld.domain.valueobject.TypeCategory;
import com.gtalent.helloworld.service.entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"type", "name"})
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String icon;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCategory type;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    protected Category() {}

    public Category(String name, String icon, TypeCategory type, User createdBy) {
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public TypeCategory getType() { return type; }
    public void setType(TypeCategory type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
