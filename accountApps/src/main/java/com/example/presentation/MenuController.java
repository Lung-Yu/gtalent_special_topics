package com.example.presentation;

import java.util.Scanner;

/**
 * 選單控制器
 * 負責處理選單邏輯和使用者輸入
 */
public class MenuController {
    private final Scanner scanner;
    private final MenuView view;
    
    public MenuController(Scanner scanner) {
        this.scanner = scanner;
        this.view = new MenuView();
    }
    
    /**
     * 啟動選單系統
     */
    public void start() {
        boolean running = true;
        
        while (running) {
            view.showMainMenu();
            
            try {
                int choice = readUserInput();
                running = handleUserChoice(choice);
            } catch (Exception e) {
                view.showMessage("輸入錯誤：" + e.getMessage());
                scanner.nextLine(); // 清除錯誤輸入
            }
        }
        
        view.showExitMessage();
    }
    
    /**
     * 讀取使用者輸入
     * @return 使用者選擇的選項代碼
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
     * @param choice 使用者選擇的選項代碼
     * @return 如果應該繼續運行返回 true，否則返回 false
     */
    private boolean handleUserChoice(int choice) {
        MenuOption option = MenuOption.fromCode(choice);
        
        if (option == null) {
            view.showInvalidInputMessage();
            return true;
        }
        
        switch (option) {
            case EXIT:
                return false;
            case EXPENDITURE:
                handleExpenditure();
                break;
            case CATEGORY_MANAGEMENT:
                handleCategoryManagement();
                break;
            case QUERY_RECORDS:
                handleQueryRecords();
                break;
        }
        
        return true;
    }
    
    /**
     * 處理支出功能
     */
    private void handleExpenditure() {
        view.showMessage("\n=== 支出功能 ===");
        // TODO: 實作支出功能邏輯
        view.showMessage("功能開發中...\n");
    }
    
    /**
     * 處理分類標籤管理
     */
    private void handleCategoryManagement() {
        view.showMessage("\n=== 分類標籤管理 ===");
        // TODO: 實作分類標籤管理邏輯
        view.showMessage("功能開發中...\n");
    }
    
    /**
     * 處理消費紀錄查詢
     */
    private void handleQueryRecords() {
        view.showMessage("\n=== 消費紀錄查詢 ===");
        // TODO: 實作消費紀錄查詢邏輯
        view.showMessage("功能開發中...\n");
    }
}
