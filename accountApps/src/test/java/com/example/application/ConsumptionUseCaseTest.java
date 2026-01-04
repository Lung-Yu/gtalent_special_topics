package com.example.application;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import com.example.application.command.ConsumptionCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.infrastructure.persistence.InMemoryExpenditureRecordRepository;

public class ConsumptionUseCaseTest {

    private ExpenditureRecordRepository expenditureRecordRepository;
    private ConsumptionUseCase consumptionUseCase;

    private User user1;
    private User user2;
    private LocalDate testDate;

    @Before
    public void setUp() {
        expenditureRecordRepository = new InMemoryExpenditureRecordRepository();
        consumptionUseCase = new ConsumptionUseCase(expenditureRecordRepository);

        user1 = new User("user1");
        user2 = new User("user2");
        testDate = LocalDate.of(2024, 1, 15);
    }

    @Test
    public void executeCreatesAndSavesExpenditureRecord() {
        ConsumptionCommand command = new ConsumptionCommand(
                "groceries", "food", 50, user1, testDate);
        consumptionUseCase.execute(command);

        ExpenditureRecord saved = expenditureRecordRepository.findAll().get(0);
        assertEquals(user1, saved.getUser());
        assertEquals("groceries", saved.getName());
        assertEquals(50, saved.getMoney());
        assertEquals("food", saved.getCategory());
        assertEquals(LocalDate.of(2024, 1, 15), saved.getDate());
    }

    @Test
    public void executeWithMultipleUsersCreatesMultipleRecords() {
        ConsumptionCommand command1 = new ConsumptionCommand(
                "groceries", "food", 50, user1, testDate);
        ConsumptionCommand command2 = new ConsumptionCommand(
                "gas", "transport", 30, user1, testDate);
        ConsumptionCommand command3 = new ConsumptionCommand(
                "restaurant", "food", 80, user2, testDate);
        ConsumptionCommand command4 = new ConsumptionCommand(
                "movie", "entertainment", 15, user2, testDate);

        consumptionUseCase.execute(command1);
        consumptionUseCase.execute(command2);
        consumptionUseCase.execute(command3);
        consumptionUseCase.execute(command4);

        assertEquals(4, expenditureRecordRepository.findAll().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeWithNegativeMoneyThrowsException() {
        ConsumptionCommand command = new ConsumptionCommand(
                "refund", "food", -50, user1, testDate);
        consumptionUseCase.execute(command);
    }
}