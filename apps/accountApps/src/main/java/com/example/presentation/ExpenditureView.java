package com.example.presentation;

import com.example.domain.model.ExpenditureRecord;
import java.util.List;

/**
 * 支出功能視圖
 * 負責顯示支出相關的介面
 */
public class ExpenditureView {
    
    /**
     * 顯示支出功能主選單
     */
    public void showExpenditureMenu() {
        System.out.println("\n=== 支出功能 ===");
        System.out.println("1. 新增支出記錄");
        System.out.println("2. 查看今日支出");
        System.out.println("0. 返回主選單");
        System.out.print("請選擇功能 (0-2): ");
    }
    
    /**
     * 提示輸入支出名稱
     */
    public void promptExpenditureName() {
        System.out.print("請輸入支出名稱: ");
    }
    
    /**
     * 提示輸入支出金額
     */
    public void promptExpenditureAmount() {
        System.out.print("請輸入金額: ");
    }
    
    /**
     * 提示輸入支付方式
     */
    public void promptPaymentMethod() {
        System.out.println("支付方式選項: LinePay, AppPay, GooglePay");
        System.out.print("請輸入支付方式: ");
    }
    
    /**
     * 提示輸入分類標籤
     */
    public void promptCategories() {
        System.out.print("請輸入分類標籤 (多個標籤請用逗號分隔): ");
    }
    
    /**
     * 顯示支出記錄列表
     */
    public void showExpenditureRecords(List<ExpenditureRecord> records) {
        if (records == null || records.isEmpty()) {
            System.out.println("目前沒有支出記錄");
            return;
        }
        
        System.out.println("\n支出記錄列表：");
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-20s %-10s %-15s %-20s %-15s%n", "名稱", "金額", "支付方式", "分類", "日期");
        System.out.println("----------------------------------------------------------------");
        
        int total = 0;
        for (ExpenditureRecord record : records) {
            System.out.printf("%-20s %-10d %-15s %-20s %-15s%n",
                truncate(record.getName(), 20),
                record.getMoney(),
                record.getPayway(),
                String.join(", ", record.getCategory()),
                record.getDate()
            );
            total += record.getMoney();
        }
        System.out.println("----------------------------------------------------------------");
        System.out.println("共 " + records.size() + " 筆記錄，總金額: " + total + " 元\n");
    }
    
    /**
     * 截斷字串以符合顯示寬度
     */
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 2) + "..";
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
        System.out.print("請輸入0~2之間的數字 : ");
    }
}
