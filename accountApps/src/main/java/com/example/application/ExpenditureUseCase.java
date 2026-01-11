package com.example.application;

import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.Category;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.service.ConsumptionService;
import com.example.domain.valueobject.PaymentMethod;
import com.example.domain.valueobject.TypeCategory;

public class ExpenditureUseCase {
    private ConsumptionService consumptionService;
    private CategoryRepository categoryRepository;

    public ExpenditureUseCase(ConsumptionService consumptionService, CategoryRepository categoryRepository){
        this.consumptionService = consumptionService;
        this.categoryRepository = categoryRepository;
    }

    public void execute(ExpenditureCommand command){
        
        validateInput(command);
        
        // 檢查並自動建立不存在的分類
        ensureCategoriesExist(command);
    
        consumptionService.execute(command);
    }
    
    private void ensureCategoriesExist(ExpenditureCommand command) {
        for (String categoryName : command.getCategory()) {
            // 檢查該分類是否已存在（OUTCOME 類型）
            if (!categoryRepository.existsByTypeAndName(categoryName, "OUTCOME")) {
                // 不存在則自動建立
                Category newCategory = new Category(categoryName, "", TypeCategory.OUTCOME, command.getUser());
                categoryRepository.save(newCategory);
            }
        }
    }
    
    private void validateInput(ExpenditureCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.getUser() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (command.getMoney() <= 0) {
            throw new IllegalArgumentException("Money must be greater than 0");
        }
        if (command.getPayway() == null || command.getPayway().isEmpty()) {
            throw new IllegalArgumentException("Payway cannot be null or empty");
        }
        if (command.getCategory() == null || command.getCategory().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        
        // Validate payway support
        try {
            PaymentMethod.valueOf(command.getPayway());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported payway: " + command.getPayway());
        }
    }


}
