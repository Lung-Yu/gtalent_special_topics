package com.example.infrastructure.persistence;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.valueobject.PaymentMethod;
import com.example.domain.valueobject.StatisticsCategory;
import com.example.domain.valueobject.UserIdentity;
import com.example.infrastructure.util.DatabaseConnectionFactory;
import org.junit.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * MySQLExpenditureRecordRepository 測試
 * 驗證 SQL 聚合查詢功能
 */
public class MySQLExpenditureRecordRepositoryTest {
    
    private static ExpenditureRecordRepository repository;
    private static UserRepository userRepository;
    private static User testUser1;
    private static User testUser2;
    private static boolean isDatabaseAvailable = false;
    
    @BeforeClass
    public static void setUp() {
        try (Connection conn = DatabaseConnectionFactory.getConnection()) {
            isDatabaseAvailable = true;
            
            // 初始化 Repository
            userRepository = new MySQLUserRepository();
            repository = new MySQLExpenditureRecordRepository(userRepository);
            
            // 創建測試使用者
            testUser1 = new User("test_user1", "password1");
            testUser2 = new User("test_user2", "password2");
            
            // 儲存測試使用者到資料庫
            userRepository.save(testUser1);
            userRepository.save(testUser2);
            
            System.out.println("✓ 測試環境初始化完成");
        } catch (Exception e) {
            System.err.println("⚠ 資料庫連線失敗，測試將被跳過");
        }
    }
    
    @Before
    public void cleanUpData() {
        assumeTrue("資料庫未啟動", isDatabaseAvailable);
        // 清空支出記錄表
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM expenditure_records WHERE username LIKE 'test_user%'")) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            fail("清理測試資料失敗: " + e.getMessage());
        }
    }
    
    @Test
    public void testSaveExpenditureRecord() {
        // Arrange
        ExpenditureRecord record = new ExpenditureRecord(
            UserIdentity.from(testUser1),
            "午餐",
            100,
            Arrays.asList("food", "salary"),
            PaymentMethod.LinePay,
            LocalDate.now()
        );
        
        // Act
        repository.save(record);
        
        // Assert
        List<ExpenditureRecord> records = repository.findByUser(testUser1);
        assertEquals(1, records.size());
        assertEquals("午餐", records.get(0).getName());
        assertEquals(100, records.get(0).getMoney());
        assertEquals(2, records.get(0).getCategory().size());
        
        System.out.println("✓ 支出記錄儲存測試通過");
    }
    
    @Test
    public void testFindByUserAndDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        ExpenditureRecord record1 = new ExpenditureRecord(
            UserIdentity.from(testUser1), "午餐", 100, Arrays.asList("food"), PaymentMethod.LinePay, today
        );
        ExpenditureRecord record2 = new ExpenditureRecord(
            UserIdentity.from(testUser1), "晚餐", 150, Arrays.asList("food"), PaymentMethod.AppPay, today
        );
        ExpenditureRecord record3 = new ExpenditureRecord(
            UserIdentity.from(testUser2), "交通", 50, Arrays.asList("salary"), PaymentMethod.GooglePay, today
        );
        
        repository.save(record1);
        repository.save(record2);
        repository.save(record3);
        
        // Act
        List<ExpenditureRecord> user1Records = repository.findByUserAndDate(testUser1, today);
        
        // Assert
        assertEquals(2, user1Records.size());
        
        System.out.println("✓ 按使用者和日期查詢測試通過");
    }
    
    @Test
    public void testFindStatisticsByDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        // 使用者1: food 300, salary 100
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "午餐", 100, Arrays.asList("food"), PaymentMethod.LinePay, today
        ));
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "晚餐", 200, Arrays.asList("food"), PaymentMethod.AppPay, today
        ));
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "薪資", 100, Arrays.asList("salary"), PaymentMethod.GooglePay, today
        ));
        
        // 使用者2: food 150, salary 250
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser2), "早餐", 150, Arrays.asList("food"), PaymentMethod.LinePay, today
        ));
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser2), "獲金", 250, Arrays.asList("salary"), PaymentMethod.AppPay, today
        ));
        
        // Act - 使用 SQL 聚合查詢
        List<StatisticsPoint> statistics = repository.findStatisticsByDate(today);
        
        // Assert
        assertEquals(4, statistics.size()); // 2 users * 2 categories
        
        // 驗證統計結果
        boolean foundUser1Food = false;
        boolean foundUser1Salary = false;
        boolean foundUser2Food = false;
        boolean foundUser2Salary = false;
        
        for (StatisticsPoint point : statistics) {
            if (point.getUsername().equals("test_user1")) {
                if (point.getCategory() == StatisticsCategory.food) {
                    assertEquals(300, point.getAmount());
                    foundUser1Food = true;
                } else if (point.getCategory() == StatisticsCategory.salary) {
                    assertEquals(100, point.getAmount());
                    foundUser1Salary = true;
                }
            } else if (point.getUsername().equals("test_user2")) {
                if (point.getCategory() == StatisticsCategory.food) {
                    assertEquals(150, point.getAmount());
                    foundUser2Food = true;
                } else if (point.getCategory() == StatisticsCategory.salary) {
                    assertEquals(250, point.getAmount());
                    foundUser2Salary = true;
                }
            }
        }
        
        assertTrue("應找到 user1 的 food 統計", foundUser1Food);
        assertTrue("應找到 user1 的 salary 統計", foundUser1Salary);
        assertTrue("應找到 user2 的 food 統計", foundUser2Food);
        assertTrue("應找到 user2 的 salary 統計", foundUser2Salary);
        
        System.out.println("✓ SQL 聚合查詢（按使用者和分類）測試通過");
    }
    
    @Test
    public void testFindStatisticsByCategoryAndDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        // 使用者1: food 300, salary 100
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "午餐", 100, Arrays.asList("food"), PaymentMethod.LinePay, today
        ));
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "晚餐", 200, Arrays.asList("food"), PaymentMethod.AppPay, today
        ));
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "薪資", 100, Arrays.asList("salary"), PaymentMethod.GooglePay, today
        ));
        
        // 使用者2: food 150, salary 250
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser2), "早餐", 150, Arrays.asList("food"), PaymentMethod.LinePay, today
        ));
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser2), "獲金", 250, Arrays.asList("salary"), PaymentMethod.AppPay, today
        ));
        
        // Act - 使用 SQL 聚合查詢（僅按分類）
        List<StatisticsPoint> statistics = repository.findStatisticsByCategoryAndDate(today);
        
        // Assert
        assertEquals(2, statistics.size()); // 2 categories (不分使用者)
        
        // 驗證統計結果
        boolean foundFood = false;
        boolean foundSalary = false;
        
        for (StatisticsPoint point : statistics) {
            assertTrue("管理者統計不應區分使用者", point.isAggregated());
            
            if (point.getCategory() == StatisticsCategory.food) {
                assertEquals(450, point.getAmount()); // 100 + 200 + 150
                foundFood = true;
            } else if (point.getCategory() == StatisticsCategory.salary) {
                assertEquals(350, point.getAmount()); // 100 + 250
                foundSalary = true;
            }
        }
        
        assertTrue("應找到 food 統計", foundFood);
        assertTrue("應找到 salary 統計", foundSalary);
        
        System.out.println("✓ SQL 聚合查詢（僅按分類）測試通過");
    }
    
    @Test
    public void testMultipleCategoriesAggregation() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        // 一筆支出有兩個分類
        repository.save(new ExpenditureRecord(
            UserIdentity.from(testUser1), "午餐兼談公事", 200, 
            Arrays.asList("food", "salary"), 
            PaymentMethod.LinePay, today
        ));
        
        // Act
        List<StatisticsPoint> statistics = repository.findStatisticsByUserAndDate(testUser1, today);
        
        // Assert
        assertEquals(2, statistics.size()); // 兩個分類都應該有
        
        // 每個分類都應該計入 200
        for (StatisticsPoint point : statistics) {
            assertEquals(200, point.getAmount());
            assertTrue(point.getCategory() == StatisticsCategory.food || 
                      point.getCategory() == StatisticsCategory.salary);
        }
        
        System.out.println("✓ 多分類支出聚合測試通過");
    }
    
    @AfterClass
    public static void tearDown() {
        // 清理測試資料
        try (Connection conn = DatabaseConnectionFactory.getConnection()) {
            conn.prepareStatement("DELETE FROM expenditure_records WHERE username LIKE 'test_user%'").executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE username LIKE 'test_user%'").executeUpdate();
        } catch (SQLException e) {
            System.err.println("清理測試資料失敗: " + e.getMessage());
        }
        
        System.out.println("✓ 測試環境清理完成");
    }
}
