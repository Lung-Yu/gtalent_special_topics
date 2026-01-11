package com.example.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.domain.model.Category;
import com.example.domain.repository.CategoryRepository;

public class InMemoryCategoryRepository implements CategoryRepository {

    private final List<Category> categories;

    public InMemoryCategoryRepository() {
        this.categories = new ArrayList<>();
    }

    public InMemoryCategoryRepository(List<Category> seed) {
        this.categories = new ArrayList<>(seed == null ? Collections.emptyList() : seed);
    }

    @Override
    public List<Category> findByName(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        return categories.stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByType(String type) {
        if (type == null) {
            return Collections.emptyList();
        }
        String normalized = type.trim().toUpperCase();
        return categories.stream()
                .filter(c -> c.getType() != null && c.getType().name().equalsIgnoreCase(normalized))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.categories.add(category);
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(categories);
    }

    @Override
    public Category findByTypeAndName(String name, String type) {
        if (name == null || type == null) {
            return null;
        }
        String normalizedType = type.trim().toUpperCase();
        return categories.stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name)
                        && c.getType() != null && c.getType().name().equalsIgnoreCase(normalizedType))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsByTypeAndName(String name, String type) {
        return findByTypeAndName(name, type) != null;
    }
}
