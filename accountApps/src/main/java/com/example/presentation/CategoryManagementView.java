package com.example.presentation;

import com.example.domain.model.Category;
import java.util.List;

/**
 * 分類標籤管理視圖
 * 負責顯示分類管理相關的介面
 */
public class CategoryManagementView {
    
    /**
     * 顯示分類管理子選單
     */
    public void showCategoryMenu() {
        System.out.println("\n=== 分類標籤管理 ===");
        System.out.println("1. 新增分類標籤");
        System.out.println("2. 查看所有分類標籤");
        System.out.println("3. 查看收入分類");
        System.out.println("4. 查看支出分類");
        System.out.println("0. 返回主選單");
        System.out.print("請選擇功能 (0-4): ");
    }
    
    /**
     * 顯示分類標籤列表
     */
    public void showCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            System.out.println("目前沒有分類標籤");
            return;
        }
        
        System.out.println("\n分類標籤列表：");
        System.out.println("----------------------------------------");
        System.out.printf("%-15s %-10s %-10s %-20s%n", "名稱", "圖示", "類型", "建立時間");
        System.out.println("----------------------------------------");
        
        for (Category category : categories) {
            System.out.printf("%-15s %-10s %-10s %-20s%n",
                category.getName(),
                category.getIcon().isEmpty() ? "-" : category.getIcon(),
                category.getType(),
                category.getCreatedAt().toString().substring(0, 19)
            );
        }
        System.out.println("----------------------------------------");
        System.out.println("共 " + categories.size() + " 個分類標籤\n");
    }
    
    /**
     * 提示輸入分類名稱
     */
    public void promptCategoryName() {
        System.out.print("請輸入分類名稱: ");
    }
    
    /**
     * 提示輸入分類類型
     */
    public void promptCategoryType() {
        System.out.print("請輸入分類類型 (INCOME/OUTCOME): ");
    }
    
    /**
     * 提示輸入分類圖示
     */
    public void promptCategoryIcon() {
        System.out.print("請輸入分類圖示 (選填，直接按 Enter 跳過): ");
    }
    
    /**
     * 顯示成功訊息
     */
    public void showSuccessMessage(String message) {
        System.out.println("✓ " + message + "\n");
    }
    
    /**
     * 顯示錯誤訊息
     */
    public void showErrorMessage(String message) {
        System.out.println("✗ 錯誤：" + message + "\n");
    }
    
    /**
     * 顯示一般訊息
     */
    public void showMessage(String message) {
        System.out.println(message);
    }
    
    /**
     * 顯示無效輸入訊息
     */
    public void showInvalidInputMessage() {
        System.out.print("請輸入0~4之間的數字 : ");
    }
}
