package com.gtalent.helloworld.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.valueobject.TypeCategory;
import com.gtalent.helloworld.repository.CategoryRepository;
import com.gtalent.helloworld.service.entities.User;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(String name, String icon, TypeCategory type, User createdBy) {
        if (categoryRepository.existsByTypeAndName(type, name)) {
            throw new IllegalArgumentException("分類已存在：type=" + type + ", name=" + name);
        }
        return categoryRepository.save(new Category(name, icon, type, createdBy));
    }

    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Category> findByType(TypeCategory type, Pageable pageable) {
        return categoryRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到分類 id=" + id));
    }

    public Category update(Long id, String name, String icon, TypeCategory type) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到分類 id=" + id));
        category.setName(name);
        category.setIcon(icon);
        category.setType(type);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
