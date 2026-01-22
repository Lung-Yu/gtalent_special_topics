package com.example.presentation;

/**
 * 選單顯示視圖
 * 負責顯示使用者介面相關的內容
 */
public class MenuView {
    
    /**
     * 顯示主選單
     */
    public void showMainMenu() {
        System.out.println("=== 記帳系統選單 ===");
        System.out.println("1. 支出功能");
        System.out.println("2. 分類標籤管理");
        System.out.println("3. 消費紀錄查詢");
        System.out.println("0. 退出系統");
        System.out.print("請選擇功能 (0-3): ");
    }
    
    /**
     * 顯示無效輸入訊息
     */
    public void showInvalidInputMessage() {
        System.out.print("請輸入0~3之間的數字 : ");
    }
    
    /**
     * 顯示退出訊息
     */
    public void showExitMessage() {
        System.out.println();
        System.out.println("感謝使用記帳系統，再見！");
    }
    
    /**
     * 顯示功能訊息
     * @param message 要顯示的訊息
     */
    public void showMessage(String message) {
        System.out.println(message);
    }
}
