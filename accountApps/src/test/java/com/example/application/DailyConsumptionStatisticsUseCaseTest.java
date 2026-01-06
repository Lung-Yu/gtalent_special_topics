package com.example.application;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.DefaultStatisticsCalculator;
import com.example.domain.service.StatisticsCalculator;
import com.example.domain.valueobject.PaymentMethod;
import com.example.domain.valueobject.StatisticsType;
import com.example.infrastructure.persistence.InMemoryExpenditureRecordRepository;
import com.example.infrastructure.persistence.InMemoryStatisticsPointRepository;
import com.example.infrastructure.persistence.InMemoryUserRepository;

/**
 * DailyConsumptionStatisticsUseCase 單元測試
 */
public class DailyConsumptionStatisticsUseCaseTest {

    private UserRepository userRepository;
    private ExpenditureRecordRepository expenditureRecordRepository;
    private StatisticsPointRepository statisticsPointRepository;
    private StatisticsCalculator statisticsCalculator;
    private DailyConsumptionStatisticsUseCase useCase;
    
    private User user1;
    private User user2;
    private LocalDate testDate;

    @Before
    public void setUp() {
        // 初始化 Repository
        userRepository = new InMemoryUserRepository();
        expenditureRecordRepository = new InMemoryExpenditureRecordRepository();
        statisticsPointRepository = new InMemoryStatisticsPointRepository();
        statisticsCalculator = new DefaultStatisticsCalculator();
        
        // 初始化 UseCase
        useCase = new DailyConsumptionStatisticsUseCase(
            expenditureRecordRepository,
            statisticsPointRepository,
            userRepository,
            statisticsCalculator
        );
        
        // 建立測試資料
        testDate = LocalDate.of(2025, 12, 30);
        user1 = new User("alice");
        user2 = new User("bob");
        
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    public void testCalculate_WithUserStatistics_ShouldGroupByUserAndCategory() {
        // Arrange: 準備測試資料
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "午餐", 100, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "晚餐", 150, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "薪水", 50000, "salary", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user2, "早餐", 50, "food", PaymentMethod.LinePay, testDate));
        
        DailyStatisticsCommand command = new DailyStatisticsCommand(testDate, StatisticsType.USER_STATISTICS);
        
        // Act: 執行統計
        useCase.calculate(command);
        
        // Assert: 驗證結果
        List<StatisticsPoint> results = statisticsPointRepository.findAll();
        assertNotNull(results);
        assertFalse("統計結果不應為空", results.isEmpty());
        
        // user1 應該有 2 筆：food (250) 和 salary (50000)
        // user2 應該有 1 筆：food (50)
        assertEquals("應該有 3 筆統計資料", 3, results.size());
        
        // 驗證 user1 的 food 統計
        StatisticsPoint user1FoodPoint = results.stream()
            .filter(p -> p.getUser() != null && 
                        p.getUser().getUsername().equals("alice") && 
                        p.getCategory().name().equals("food"))
            .findFirst()
            .orElse(null);
        assertNotNull("應該有 alice 的 food 統計", user1FoodPoint);
        assertEquals("alice 的 food 總額應為 250", 250, user1FoodPoint.getAmount());
        
        // 驗證 user2 的 food 統計
        StatisticsPoint user2FoodPoint = results.stream()
            .filter(p -> p.getUser() != null && 
                        p.getUser().getUsername().equals("bob") && 
                        p.getCategory().name().equals("food"))
            .findFirst()
            .orElse(null);
        assertNotNull("應該有 bob 的 food 統計", user2FoodPoint);
        assertEquals("bob 的 food 總額應為 50", 50, user2FoodPoint.getAmount());
    }

    @Test
    public void testCalculate_WithManagerStatistics_ShouldGroupByCategoryOnly() {
        // Arrange: 準備測試資料
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "午餐", 100, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "晚餐", 150, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user2, "早餐", 50, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "薪水", 50000, "salary", PaymentMethod.LinePay, testDate));
        
        DailyStatisticsCommand command = new DailyStatisticsCommand(testDate, StatisticsType.MANAGER_STATISTICS);
        
        // Act: 執行統計
        useCase.calculate(command);
        
        // Assert: 驗證結果
        List<StatisticsPoint> results = statisticsPointRepository.findAll();
        assertNotNull(results);
        assertFalse("統計結果不應為空", results.isEmpty());
        
        // 管理者統計只按類別分組，不分使用者
        // 應該有 2 筆：food (300) 和 salary (50000)
        assertEquals("應該有 2 筆統計資料", 2, results.size());
        
        // 驗證 food 總額
        StatisticsPoint foodPoint = results.stream()
            .filter(p -> p.getCategory().name().equals("food"))
            .findFirst()
            .orElse(null);
        assertNotNull("應該有 food 統計", foodPoint);
        assertEquals("food 總額應為 300", 300, foodPoint.getAmount());
        assertNull("管理者統計的 user 應為 null", foodPoint.getUser());
        
        // 驗證 salary 總額
        StatisticsPoint salaryPoint = results.stream()
            .filter(p -> p.getCategory().name().equals("salary"))
            .findFirst()
            .orElse(null);
        assertNotNull("應該有 salary 統計", salaryPoint);
        assertEquals("salary 總額應為 50000", 50000, salaryPoint.getAmount());
    }

    @Test
    public void testCalculate_WithNoRecords_ShouldReturnEmptyStatistics() {
        // Arrange: 不新增任何消費記錄
        DailyStatisticsCommand command = new DailyStatisticsCommand(testDate, StatisticsType.USER_STATISTICS);
        
        // Act: 執行統計
        useCase.calculate(command);
        
        // Assert: 驗證結果應為空
        List<StatisticsPoint> results = statisticsPointRepository.findAll();
        assertNotNull(results);
        assertTrue("無消費記錄時，統計結果應為空", results.isEmpty());
    }

    @Test
    public void testCalculate_WithDifferentDate_ShouldOnlyCountMatchingDate() {
        // Arrange: 準備不同日期的資料
        LocalDate yesterday = testDate.minusDays(1);
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "昨天午餐", 100, "food", PaymentMethod.LinePay, yesterday));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "今天午餐", 200, "food", PaymentMethod.LinePay, testDate));
        
        DailyStatisticsCommand command = new DailyStatisticsCommand(testDate, StatisticsType.USER_STATISTICS);
        
        // Act: 執行統計
        useCase.calculate(command);
        
        // Assert: 只應統計指定日期的資料
        List<StatisticsPoint> results = statisticsPointRepository.findAll();
        assertEquals("應該只有 1 筆統計資料", 1, results.size());
        assertEquals("金額應為今天的 200", 200, results.get(0).getAmount());
    }

    @Test
    public void testCalculate_DefaultCommandType_ShouldUseUserStatistics() {
        // Arrange: 使用預設建構子（預設為使用者統計）
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "午餐", 100, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user2, "午餐", 150, "food", PaymentMethod.LinePay, testDate));
        
        DailyStatisticsCommand command = new DailyStatisticsCommand(testDate);
        
        // Assert: 確認預設類型為 USER_STATISTICS
        assertEquals(StatisticsType.USER_STATISTICS, command.getStatisticsType());
        
        // Act: 執行統計
        useCase.calculate(command);
        
        // Assert: 應該有按使用者分組的結果（2 筆）
        List<StatisticsPoint> results = statisticsPointRepository.findAll();
        assertEquals("使用者統計應有 2 筆（每個使用者各 1 筆）", 2, results.size());
    }

    @Test
    public void testCalculate_MultipleCategories_ShouldGroupCorrectly() {
        // Arrange: 同一使用者有多種類別的消費
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "午餐", 100, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "晚餐", 200, "food", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "獎金", 10000, "salary", PaymentMethod.LinePay, testDate));
        expenditureRecordRepository.save(new ExpenditureRecord(user1, "年終", 30000, "salary", PaymentMethod.LinePay, testDate));
        
        DailyStatisticsCommand command = new DailyStatisticsCommand(testDate, StatisticsType.USER_STATISTICS);
        
        // Act: 執行統計
        useCase.calculate(command);
        
        // Assert: 應該有 2 筆統計（food 和 salary）
        List<StatisticsPoint> results = statisticsPointRepository.findAll();
        assertEquals("應該有 2 筆統計資料", 2, results.size());
        
        // 驗證各類別總額
        int foodTotal = results.stream()
            .filter(p -> p.getCategory().name().equals("food"))
            .mapToInt(StatisticsPoint::getAmount)
            .sum();
        assertEquals("food 總額應為 300", 300, foodTotal);
        
        int salaryTotal = results.stream()
            .filter(p -> p.getCategory().name().equals("salary"))
            .mapToInt(StatisticsPoint::getAmount)
            .sum();
        assertEquals("salary 總額應為 40000", 40000, salaryTotal);
    }
}
