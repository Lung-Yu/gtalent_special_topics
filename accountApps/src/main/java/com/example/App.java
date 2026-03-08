package com.example;

import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.UserRepository;
import com.example.infrastructure.persistence.InMemoryCategoryRepository;
import com.example.infrastructure.persistence.InMemoryExpenditureRecordRepository;
import com.example.infrastructure.persistence.MySQLExpenditureRecordRepository;
import com.example.infrastructure.persistence.MySQLUserRepository;
import com.example.infrastructure.util.DatabaseConnectionFactory;
import com.example.presentation.LoginController;
import com.example.presentation.MenuController;
import java.util.Scanner;

/**
 * 記帳系統主程式
 * 應用程式入口點，負責初始化和資源管理
 */
public class App {
    private final Scanner scanner;
    private MenuController menuController;
    private final CategoryRepository categoryRepository;
    private final ExpenditureRecordRepository expenditureRecordRepository;
    private final UserRepository userRepository;
    private User currentUser;

    /**
     * 建構子 - 初始化應用程式資源
     * 可透過環境變數 USE_MYSQL_EXPENDITURE=true 切換支出記錄儲存方式
     * - true: 使用 MySQL 資料庫（預設，支援 SQL 聚合優化）
     * - false: 使用記憶體儲存（適合測試）
     */
    public App() {
        this.scanner = new Scanner(System.in);
        this.categoryRepository = new InMemoryCategoryRepository();
        
        // 使用 MySQL 資料庫驗證
        this.userRepository = new MySQLUserRepository();
        
        // 根據環境變數決定使用哪種支出記錄儲存實作
        String useMySQLExpenditure = System.getenv("USE_MYSQL_EXPENDITURE");
        boolean shouldUseMySQLExpenditure = useMySQLExpenditure == null || 
                                           !useMySQLExpenditure.equalsIgnoreCase("false");
        
        if (shouldUseMySQLExpenditure) {
            this.expenditureRecordRepository = new MySQLExpenditureRecordRepository(userRepository);
            System.out.println("✓ 使用 MySQL 儲存支出記錄（支援 SQL 聚合優化）");
        } else {
            this.expenditureRecordRepository = new InMemoryExpenditureRecordRepository();
            System.out.println("✓ 使用記憶體儲存支出記錄");
        }
    }

    /**
     * 啟動應用程式
     */
    public void start() {
        try {
            // 執行登入流程
            LoginController loginController = new LoginController(scanner, userRepository);
            currentUser = loginController.login();
            
            if (currentUser == null) {
                System.out.println("登入失敗，系統即將關閉。");
                return;
            }
            
            // 登入成功後初始化主選單控制器
            menuController = new MenuController(
                scanner, categoryRepository, expenditureRecordRepository, userRepository, currentUser);
            
            // 啟動主選單
            menuController.start();
            
        } catch (Exception e) {
            System.err.println("系統發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 關閉應用程式資源
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
        // 關閉資料庫連接池
        DatabaseConnectionFactory.closeDataSource();
    }

    /**
     * 主程式入口
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        App app = new App();
        try {
            app.start();
        } finally {
            app.close();
        }
    }
}
