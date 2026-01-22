package com.example.presentation;

import com.example.application.ExpenditureUseCase;
import com.example.application.ExpenditureQueryUseCase;
import com.example.application.command.ExpenditureCommand;
import com.example.application.command.ExpenditureQueryCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.service.ConsumptionService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * 支出功能控制器
 * 處理支出記錄相關的業務邏輯
 */
public class ExpenditureController {
    private final Scanner scanner;
    private final ExpenditureView view;
    private final ExpenditureUseCase expenditureUseCase;
    private final ExpenditureQueryUseCase queryUseCase;
    private final User currentUser;
    
    public ExpenditureController(
            Scanner scanner, 
            CategoryRepository categoryRepository,
            ExpenditureRecordRepository expenditureRecordRepository,
            User currentUser) {
        this.scanner = scanner;
        this.view = new ExpenditureView();
        
        ConsumptionService consumptionService = new ConsumptionService(expenditureRecordRepository);
        this.expenditureUseCase = new ExpenditureUseCase(consumptionService, categoryRepository);
        this.queryUseCase = new ExpenditureQueryUseCase(expenditureRecordRepository);
        this.currentUser = currentUser;
    }
    
    /**
     * 啟動支出功能選單
     */
    public void start() {
        boolean running = true;
        
        while (running) {
            view.showExpenditureMenu();
            
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
        ExpenditureMenuOption option = ExpenditureMenuOption.fromCode(choice);
        
        if (option == null) {
            view.showInvalidInputMessage();
            return true;
        }
        
        switch (option) {
            case BACK:
                return false;
            case CREATE:
                handleCreateExpenditure();
                break;
            case VIEW_TODAY:
                handleViewTodayExpenditure();
                break;
        }
        
        return true;
    }
    
    /**
     * 處理新增支出記錄
     */
    private void handleCreateExpenditure() {
        try {
            view.showMessage("\n--- 新增支出記錄 ---");
            scanner.nextLine(); // 清除 buffer
            
            // 讀取支出名稱
            view.promptExpenditureName();
            String name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                view.showErrorMessage("支出名稱不能為空");
                return;
            }
            
            // 讀取金額
            view.promptExpenditureAmount();
            int amount;
            try {
                amount = Integer.parseInt(scanner.nextLine().trim());
                if (amount <= 0) {
                    view.showErrorMessage("金額必須大於 0");
                    return;
                }
            } catch (NumberFormatException e) {
                view.showErrorMessage("請輸入有效的數字");
                return;
            }
            
            // 讀取支付方式
            view.promptPaymentMethod();
            String paymentMethod = scanner.nextLine().trim();
            
            if (!isValidPaymentMethod(paymentMethod)) {
                view.showErrorMessage("無效的支付方式，請輸入: LinePay, AppPay 或 GooglePay");
                return;
            }
            
            // 讀取分類標籤
            view.promptCategories();
            String categoriesInput = scanner.nextLine().trim();
            
            if (categoriesInput.isEmpty()) {
                view.showErrorMessage("至少需要一個分類標籤");
                return;
            }
            
            List<String> categories = Arrays.stream(categoriesInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            
            if (categories.isEmpty()) {
                view.showErrorMessage("至少需要一個有效的分類標籤");
                return;
            }
            
            // 建立命令並執行
            ExpenditureCommand command = new ExpenditureCommand();
            command.setUser(currentUser);
            command.setName(name);
            command.setMoney(amount);
            command.setPayway(paymentMethod);
            command.setCategory(categories);
            
            expenditureUseCase.execute(command);
            view.showSuccessMessage("支出記錄新增成功！金額: " + amount + " 元");
            
        } catch (IllegalArgumentException e) {
            view.showErrorMessage(e.getMessage());
        } catch (Exception e) {
            view.showErrorMessage("新增支出記錄時發生錯誤：" + e.getMessage());
        }
    }
    
    /**
     * 處理查看今日支出
     */
    private void handleViewTodayExpenditure() {
        try {
            ExpenditureQueryCommand command = new ExpenditureQueryCommand();
            command.setUser(currentUser);
            command.setDate(LocalDate.now());
            
            List<ExpenditureRecord> records = queryUseCase.query(command);
            
            view.showMessage("\n--- 今日支出 (" + LocalDate.now() + ") ---");
            view.showExpenditureRecords(records);
            
        } catch (Exception e) {
            view.showErrorMessage("查詢記錄時發生錯誤：" + e.getMessage());
        }
    }
    
    /**
     * 驗證支付方式是否有效
     */
    private boolean isValidPaymentMethod(String method) {
        return method.equals("LinePay") || method.equals("AppPay") || method.equals("GooglePay");
    }
}
