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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    // xls(2003), xlsx(after 2003)
    private void generate_excel() {
        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream fileOut = new FileOutputStream("workbook.xlsx")) {
            Sheet sheet = workbook.createSheet("SampleSheet");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Name");
            row.createCell(1).setCellValue("Age");

            // Save the file
            workbook.write(fileOut);
            System.out.println("Excel file created!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read_from_excel() {
        try (FileInputStream fis = new FileInputStream(new File("AACC.xlsx"));
                Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("SampleSheet");

            for (Row row : sheet) {
                boolean has_rows = false;

                for (Cell cell : row) {
                    if (cell.toString().trim().length() <= 0) {
                        break;
                    }
                    has_rows = true;
                    // Print cell value based on type
                    System.out.print(cell.toString() + "\t");
                }
                if(has_rows)
                    System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 啟動應用程式
     */
    public void start() {

        generate_excel();

        read_from_excel();

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
     * 
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
