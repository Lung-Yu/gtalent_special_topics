package com.example.presentation;

import com.example.application.CategoryUseCase;
import com.example.application.command.CategoryCreateCommand;
import com.example.application.exception.CategoryTypeNotExists;
import com.example.application.exception.DuplicateCategoryException;
import com.example.domain.model.Category;
import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;

import java.util.List;
import java.util.Scanner;

/**
 * 分類標籤管理控制器
 * 處理分類標籤相關的業務邏輯
 */
public class CategoryController {
    private final Scanner scanner;
    private final CategoryManagementView view;
    private final CategoryUseCase categoryUseCase;
    private final CategoryRepository categoryRepository;
    private final User currentUser;
    
    public CategoryController(Scanner scanner, CategoryRepository categoryRepository, User currentUser) {
        this.scanner = scanner;
        this.view = new CategoryManagementView();
        this.categoryUseCase = new CategoryUseCase(categoryRepository);
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }
    
    /**
     * 啟動分類管理選單
     */
    public void start() {
        boolean running = true;
        
        while (running) {
            view.showCategoryMenu();
            
            try {
                int choice = readUserInput();
                running = handleUserChoice(choice);
            } catch (Exception e) {
                view.showErrorMessage("輸入錯誤：" + e.getMessage());
                scanner.nextLine(); // 清除錯誤輸入
            }
        }
    }
    
    /**
     * 讀取使用者輸入
     */
    private int readUserInput() {
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }
        scanner.next(); // 清除非整數輸入
        throw new IllegalArgumentException("請輸入有效的數字");
    }
    
    /**
     * 處理使用者選擇
     */
    private boolean handleUserChoice(int choice) {
        CategoryMenuOption option = CategoryMenuOption.fromCode(choice);
        
        if (option == null) {
            view.showInvalidInputMessage();
            return true;
        }
        
        switch (option) {
            case BACK:
                return false;
            case CREATE:
                handleCreateCategory();
                break;
            case VIEW_ALL:
                handleViewAllCategories();
                break;
            case VIEW_INCOME:
                handleViewCategoriesByType("INCOME");
                break;
            case VIEW_OUTCOME:
                handleViewCategoriesByType("OUTCOME");
                break;
        }
        
        return true;
    }
    
    /**
     * 處理新增分類標籤
     */
    private void handleCreateCategory() {
        try {
            view.showMessage("\n--- 新增分類標籤 ---");
            
            // 讀取分類名稱
            view.promptCategoryName();
            scanner.nextLine(); // 清除 buffer
            String name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                view.showErrorMessage("分類名稱不能為空");
                return;
            }
            
            // 讀取分類類型
            view.promptCategoryType();
            String type = scanner.nextLine().trim().toUpperCase();
            
            if (!type.equals("INCOME") && !type.equals("OUTCOME")) {
                view.showErrorMessage("分類類型只能是 INCOME 或 OUTCOME");
                return;
            }
            
            // 讀取分類圖示（選填）
            view.promptCategoryIcon();
            String icon = scanner.nextLine().trim();
            
            // 建立命令並執行
            CategoryCreateCommand command = new CategoryCreateCommand(
                currentUser,
                name,
                type,
                icon
            );
            
            categoryUseCase.execute(command);
            view.showSuccessMessage("分類標籤「" + name + "」新增成功！");
            
        } catch (DuplicateCategoryException e) {
            view.showErrorMessage("此分類標籤已存在：" + e.getMessage());
        } catch (CategoryTypeNotExists e) {
            view.showErrorMessage("無效的分類類型：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            view.showErrorMessage(e.getMessage());
        } catch (Exception e) {
            view.showErrorMessage("新增分類標籤時發生錯誤：" + e.getMessage());
        }
    }
    
    /**
     * 處理查看所有分類標籤
     */
    private void handleViewAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            view.showCategories(categories);
        } catch (Exception e) {
            view.showErrorMessage("查詢分類標籤時發生錯誤：" + e.getMessage());
        }
    }
    
    /**
     * 處理按類型查看分類標籤
     */
    private void handleViewCategoriesByType(String type) {
        try {
            List<Category> categories = categoryRepository.findByType(type);
            view.showMessage("\n=== " + (type.equals("INCOME") ? "收入" : "支出") + "分類 ===");
            view.showCategories(categories);
        } catch (Exception e) {
            view.showErrorMessage("查詢分類標籤時發生錯誤：" + e.getMessage());
        }
    }
}
