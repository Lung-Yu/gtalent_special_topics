package com.example.domain.repository;

import java.util.List;

import com.example.domain.model.Category;

public interface CategoryRepository {
    List<Category> findByName(String name);
    List<Category> findByType(String type);
    void save(Category category);
    List<Category> findAll();
    Category findByTypeAndName(String name, String type);
    boolean existsByTypeAndName(String name, String type);
}
