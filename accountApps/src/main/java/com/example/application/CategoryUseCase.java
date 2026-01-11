package com.example.application;

import com.example.application.command.CategoryCreateCommand;
import com.example.application.exception.DuplicateCategoryException;
import com.example.application.exception.CategoryTypeNotExists;
import com.example.domain.model.Category;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.valueobject.TypeCategory;

public class CategoryUseCase {
    private final CategoryRepository categoryRepository;

    public CategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void execute(CategoryCreateCommand command) throws CategoryTypeNotExists {
        validate(command);

        TypeCategory type;
        try {
            type = TypeCategory.fromString(command.getType());
        } catch (IllegalArgumentException ex) {
            throw new CategoryTypeNotExists(command.getType());
        }

        // 檢查是否已存在相同名稱和類型的標籤
        if (categoryRepository.existsByTypeAndName(command.getName(), command.getType())) {
            throw new DuplicateCategoryException(command.getName(), command.getType());
        }

        Category category = new Category(command.getName(), command.getIcon(), type, command.getUser());
        categoryRepository.save(category);
    }

    private void validate(CategoryCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (command.getType() == null || command.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Category type cannot be null or empty");
        }
    }
}
