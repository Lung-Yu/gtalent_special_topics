package com.example.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.Category;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.service.ConsumptionService;
import com.example.domain.valueobject.PaymentMethod;
import com.example.infrastructure.persistence.InMemoryCategoryRepository;
import com.example.infrastructure.persistence.InMemoryExpenditureRecordRepository;

public class ExpenditureUseCaseTest {

    private ExpenditureRecordRepository expenditureRecordRepository;
    private CategoryRepository categoryRepository;
    private ConsumptionService consumptionService;
    private ExpenditureUseCase expenditureUseCase;

    private User user1;
    private User user2;

    @Before
    public void setUp() {
        expenditureRecordRepository = new InMemoryExpenditureRecordRepository();
        categoryRepository = new InMemoryCategoryRepository();
        consumptionService = new ConsumptionService(expenditureRecordRepository);
        expenditureUseCase = new ExpenditureUseCase(consumptionService, categoryRepository);

        user1 = new User("user1");
        user2 = new User("user2");
    }

    @Test
    public void executeCreatesAndSavesExpenditureRecord() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory(Arrays.asList("food"));

        expenditureUseCase.execute(command);

        ExpenditureRecord saved = expenditureRecordRepository.findAll().get(0);
        assertEquals(user1, saved.getUser());
        assertEquals(100, saved.getMoney());
        assertEquals(Arrays.asList("food"), saved.getCategory());
        assertNotNull(saved.getDate());
        
        // 驗證分類有被自動建立
        assertTrue(categoryRepository.existsByTypeAndName("food", "OUTCOME"));
    }

    @Test
    public void executeWithDifferentPayways() {
        String[] payways = { "LinePay", "AppPay", "GooglePay" };

        int count_expect = 1;
        for (String payway : payways) {
            expenditureUseCase = new ExpenditureUseCase(consumptionService, categoryRepository);

            ExpenditureCommand command = new ExpenditureCommand();
            command.setUser(user1);
            command.setMoney(50);
            command.setPayway(payway);
            command.setCategory(Arrays.asList("shopping"));

            expenditureUseCase.execute(command);

            assertEquals(count_expect, expenditureRecordRepository.findAll().size());
            assertEquals(PaymentMethod.valueOf(payway), expenditureRecordRepository.findAll().get(count_expect - 1).getPayway());

            count_expect++;
        }
    }

    @Test
    public void executeThrowsExceptionWhenCommandIsNull() {
        try {
            expenditureUseCase.execute(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Command cannot be null", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenUserIsNull() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(null);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory(Arrays.asList("food"));

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("User cannot be null", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenMoneyIsZero() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(0);
        command.setPayway("LinePay");
        command.setCategory(Arrays.asList("food"));

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Money must be greater than 0", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenMoneyIsNegative() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(-50);
        command.setPayway("LinePay");
        command.setCategory(Arrays.asList("food"));

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Money must be greater than 0", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenPaywayIsNull() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway(null);
        command.setCategory(Arrays.asList("food"));

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Payway cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenPaywayIsUnsupported() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("UnknownPay");
        command.setCategory(Arrays.asList("food"));

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported payway: UnknownPay", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenCategoryIsNull() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory(null);

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void executeThrowsExceptionWhenCategoryIsEmpty() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory(Collections.emptyList());

        try {
            expenditureUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void executeWithMultipleUsers() {
        ExpenditureCommand command1 = new ExpenditureCommand();
        command1.setUser(user1);
        command1.setMoney(100);
        command1.setPayway("LinePay");
        command1.setCategory(Arrays.asList("food"));

        ExpenditureCommand command2 = new ExpenditureCommand();
        command2.setUser(user2);
        command2.setMoney(200);
        command2.setPayway("AppPay");
        command2.setCategory(Arrays.asList("shopping"));

        expenditureUseCase.execute(command1);
        expenditureUseCase.execute(command2);

        assertEquals(2, expenditureRecordRepository.findAll().size());
        assertEquals(user1, expenditureRecordRepository.findAll().get(0).getUser());
        assertEquals(user2, expenditureRecordRepository.findAll().get(1).getUser());
    }

    @Test
    public void executeWithDifferentCategories() {
        String[] categories = { "food", "shopping", "entertainment", "transport" };

        int count_expect = 1;
        for (String category : categories) {
            expenditureUseCase = new ExpenditureUseCase(consumptionService, categoryRepository);

            ExpenditureCommand command = new ExpenditureCommand();
            command.setUser(user1);
            command.setMoney(50);
            command.setPayway("LinePay");
            command.setCategory(Arrays.asList(category));

            expenditureUseCase.execute(command);

            assertEquals(count_expect, expenditureRecordRepository.findAll().size());
            assertEquals(Arrays.asList(category), expenditureRecordRepository.findAll().get(count_expect - 1).getCategory());

            count_expect++;
        }
    }
    
    @Test
    public void executeWithMultipleCategories_ShouldAutoCreateNonExistentCategories() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory(Arrays.asList("food", "dining", "restaurant"));

        // 確認這些分類未存在
        assertEquals(0, categoryRepository.findAll().size());

        expenditureUseCase.execute(command);

        // 確認支出記錄被建立
        ExpenditureRecord saved = expenditureRecordRepository.findAll().get(0);
        assertEquals(Arrays.asList("food", "dining", "restaurant"), saved.getCategory());

        // 確認所有分類都被自動建立
        assertEquals(3, categoryRepository.findAll().size());
        assertTrue(categoryRepository.existsByTypeAndName("food", "OUTCOME"));
        assertTrue(categoryRepository.existsByTypeAndName("dining", "OUTCOME"));
        assertTrue(categoryRepository.existsByTypeAndName("restaurant", "OUTCOME"));
    }
    
    @Test
    public void executeWithExistingCategory_ShouldNotCreateDuplicate() {
        // 預先建立一個分類
        Category existingCategory = new Category("food", "🍔", com.example.domain.valueobject.TypeCategory.OUTCOME);
        categoryRepository.save(existingCategory);
        
        assertEquals(1, categoryRepository.findAll().size());

        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory(Arrays.asList("food", "newCategory"));

        expenditureUseCase.execute(command);

        // 應該只新增 newCategory，food 不應重複
        assertEquals(2, categoryRepository.findAll().size());
        assertTrue(categoryRepository.existsByTypeAndName("food", "OUTCOME"));
        assertTrue(categoryRepository.existsByTypeAndName("newCategory", "OUTCOME"));
    }
}
