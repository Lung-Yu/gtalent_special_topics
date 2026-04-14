package com.example.presentation;

import com.example.domain.model.ExpenditureRecord;
import java.time.LocalDate;
import java.util.List;

/**
 * 消費紀錄查詢視圖
 * 負責顯示查詢相關的介面
 */
public class ExpenditureQueryView {
    
    /**
     * 顯示查詢功能主選單
     */
    public void showQueryMenu() {
        System.out.println("\n=== 消費紀錄查詢 ===");
        System.out.println("1. 查看所有記錄");
        System.out.println("2. 按日期查詢");
        System.out.println("0. 返回主選單");
        System.out.print("請選擇功能 (0-2): ");
    }
    
    /**
     * 提示輸入日期
     */
    public void promptDate() {
        System.out.print("請輸入日期 (格式: YYYY-MM-DD，例如: 2026-01-22): ");
    }
    
    /**
     * 顯示查詢結果
     */
    public void showQueryResults(List<ExpenditureRecord> records, LocalDate date) {
        String title = date != null ? "查詢日期: " + date : "所有記錄";
        System.out.println("\n" + title);
        showExpenditureRecords(records);
    }
    
    /**
     * 顯示支出記錄列表
     */
    public void showExpenditureRecords(List<ExpenditureRecord> records) {
        if (records == null || records.isEmpty()) {
            System.out.println("查無記錄\n");
            return;
        }
        
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-20s %-10s %-15s %-20s %-15s%n", "名稱", "金額", "支付方式", "分類", "日期");
        System.out.println("----------------------------------------------------------------");
        
        int total = 0;
        for (ExpenditureRecord record : records) {
            System.out.printf("%-20s %-10d %-15s %-20s %-15s%n",
                truncate(record.getName(), 20),
                record.getMoney(),
                record.getPayway(),
                truncate(String.join(", ", record.getCategory()), 20),
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
