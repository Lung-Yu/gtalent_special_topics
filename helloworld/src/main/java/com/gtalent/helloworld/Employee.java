package com.gtalent.helloworld;

import java.time.LocalDateTime;

import org.springframework.lang.Nullable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Employee {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private @Nullable Integer id;

    private String name;

    private String age;

    private LocalDateTime createdAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
