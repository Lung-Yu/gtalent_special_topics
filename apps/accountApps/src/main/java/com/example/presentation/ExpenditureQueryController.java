package com.example.presentation;

import com.example.application.ExpenditureQueryUseCase;
import com.example.application.command.ExpenditureQueryCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * 消費紀錄查詢控制器
 * 處理查詢相關的業務邏輯
 */
public class ExpenditureQueryController {
    private final Scanner scanner;
    private final ExpenditureQueryView view;
    private final ExpenditureQueryUseCase queryUseCase;
    private final User currentUser;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ExpenditureQueryController(
            Scanner scanner, 
            ExpenditureRecordRepository expenditureRecordRepository,
            User currentUser) {
        this.scanner = scanner;
        this.view = new ExpenditureQueryView();
        this.queryUseCase = new ExpenditureQueryUseCase(expenditureRecordRepository);
        this.currentUser = currentUser;
    }
    
    /**
     * 啟動查詢功能選單
     */
    public void start() {
        boolean running = true;
        
        while (running) {
            view.showQueryMenu();
            
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
        QueryMenuOption option = QueryMenuOption.fromCode(choice);
        
        if (option == null) {
            view.showInvalidInputMessage();
            return true;
        }
        
        switch (option) {
            case BACK:
                return false;
            case VIEW_ALL:
                handleViewAllRecords();
                break;
            case VIEW_BY_DATE:
                handleViewByDate();
                break;
        }
        
        return true;
    }
    
    /**
     * 處理查看所有記錄
     */
    private void handleViewAllRecords() {
        try {
            ExpenditureQueryCommand command = new ExpenditureQueryCommand();
            command.setUser(currentUser);
            
            List<ExpenditureRecord> records = queryUseCase.query(command);
            view.showQueryResults(records, null);
            
        } catch (Exception e) {
            view.showErrorMessage("查詢記錄時發生錯誤：" + e.getMessage());
        }
    }
    
    /**
     * 處理按日期查詢
     */
    private void handleViewByDate() {
        try {
            scanner.nextLine(); // 清除 buffer
            view.promptDate();
            String dateInput = scanner.nextLine().trim();
            
            if (dateInput.isEmpty()) {
                view.showErrorMessage("日期不能為空");
                return;
            }
            
            LocalDate date;
            try {
                date = LocalDate.parse(dateInput, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                view.showErrorMessage("日期格式錯誤，請使用格式: YYYY-MM-DD (例如: 2026-01-22)");
                return;
            }
            
            ExpenditureQueryCommand command = new ExpenditureQueryCommand();
            command.setUser(currentUser);
            command.setDate(date);
            
            List<ExpenditureRecord> records = queryUseCase.query(command);
            view.showQueryResults(records, date);
            
        } catch (Exception e) {
            view.showErrorMessage("查詢記錄時發生錯誤：" + e.getMessage());
        }
    }
}
