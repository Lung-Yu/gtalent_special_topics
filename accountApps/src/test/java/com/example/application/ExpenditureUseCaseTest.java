package com.example.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.service.ConsumptionService;
import com.example.infrastructure.persistence.InMemoryExpenditureRecordRepository;

public class ExpenditureUseCaseTest {

    private ExpenditureRecordRepository expenditureRecordRepository;
    private ConsumptionService consumptionService;
    private ExpenditureUseCase expenditureUseCase;

    private User user1;
    private User user2;

    @Before
    public void setUp() {
        expenditureRecordRepository = new InMemoryExpenditureRecordRepository();
        consumptionService = new ConsumptionService(expenditureRecordRepository);
        expenditureUseCase = new ExpenditureUseCase(consumptionService);

        user1 = new User("user1");
        user2 = new User("user2");
    }

    @Test
    public void executeCreatesAndSavesExpenditureRecord() {
        ExpenditureCommand command = new ExpenditureCommand();
        command.setUser(user1);
        command.setMoney(100);
        command.setPayway("LinePay");
        command.setCategory("food");

        expenditureUseCase.execute(command);

        ExpenditureRecord saved = expenditureRecordRepository.findAll().get(0);
        assertEquals(user1, saved.getUser());
        assertEquals(100, saved.getMoney());
        assertEquals("food", saved.getCategory());
        assertNotNull(saved.getDate());
    }

    @Test
    public void executeWithDifferentPayways() {
        String[] payways = { "LinePay", "AppPay", "GooglePay" };

        int count_expect = 1;
        for (String payway : payways) {
            expenditureUseCase = new ExpenditureUseCase(consumptionService);

            ExpenditureCommand command = new ExpenditureCommand();
            command.setUser(user1);
            command.setMoney(50);
            command.setPayway(payway);
            command.setCategory("shopping");

            expenditureUseCase.execute(command);

            assertEquals(count_expect, expenditureRecordRepository.findAll().size());
            assertEquals(payway, expenditureRecordRepository.findAll().get(count_expect - 1).getPayway());

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
        command.setCategory("food");

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
        command.setCategory("food");

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
        command.setCategory("food");

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
        command.setCategory("food");

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
        command.setCategory("food");

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
        command.setCategory("");

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
        command1.setCategory("food");

        ExpenditureCommand command2 = new ExpenditureCommand();
        command2.setUser(user2);
        command2.setMoney(200);
        command2.setPayway("AppPay");
        command2.setCategory("shopping");

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
            expenditureUseCase = new ExpenditureUseCase(consumptionService);

            ExpenditureCommand command = new ExpenditureCommand();
            command.setUser(user1);
            command.setMoney(50);
            command.setPayway("LinePay");
            command.setCategory(category);

            expenditureUseCase.execute(command);

            assertEquals(count_expect, expenditureRecordRepository.findAll().size());
            assertEquals(category, expenditureRecordRepository.findAll().get(count_expect - 1).getCategory());

            count_expect++;
        }
    }
}
