package com.example.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.example.application.command.ExpenditureQueryCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.valueobject.PaymentMethod;
import com.example.infrastructure.persistence.InMemoryExpenditureRecordRepository;

public class ExpenditureQueryUseCaseTest {

    private ExpenditureRecordRepository repository;
    private ExpenditureQueryUseCase queryUseCase;

    private User userA;
    private User userB;

    @Before
    public void setUp() {
        repository = new InMemoryExpenditureRecordRepository();
        queryUseCase = new ExpenditureQueryUseCase(repository);

        userA = new User("userA");
        userB = new User("userB");

        // Seed data for queries
        repository.save(new ExpenditureRecord(userA, "breakfast", 120, "food", PaymentMethod.LinePay, LocalDate.of(2025, 12, 25)));
        repository.save(new ExpenditureRecord(userA, "lunch", 250, "food", PaymentMethod.AppPay, LocalDate.of(2025, 12, 26)));
        repository.save(new ExpenditureRecord(userB, "movie", 300, "entertainment", PaymentMethod.GooglePay, LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void queryReturnsRecordsForUser() {
        ExpenditureQueryCommand command = new ExpenditureQueryCommand();
        command.setUser(userA);

        List<ExpenditureRecord> results = queryUseCase.query(command);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(record -> record.getUser().equals(userA)));
    }

    @Test
    public void queryFiltersByDateWhenProvided() {
        ExpenditureQueryCommand command = new ExpenditureQueryCommand();
        command.setUser(userA);
        command.setDate(LocalDate.of(2025, 12, 25));

        List<ExpenditureRecord> results = queryUseCase.query(command);

        assertEquals(1, results.size());
        assertEquals("breakfast", results.get(0).getName());
        assertEquals(LocalDate.of(2025, 12, 25), results.get(0).getDate());
    }

    @Test
    public void queryReturnsEmptyListWhenNoRecordsMatch() {
        ExpenditureQueryCommand command = new ExpenditureQueryCommand();
        command.setUser(new User("userC"));

        List<ExpenditureRecord> results = queryUseCase.query(command);

        assertTrue(results.isEmpty());
    }

    @Test
    public void queryThrowsWhenCommandIsNull() {
        try {
            queryUseCase.query(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Command cannot be null", e.getMessage());
        }
    }

    @Test
    public void queryThrowsWhenUserIsNull() {
        ExpenditureQueryCommand command = new ExpenditureQueryCommand();
        command.setUser(null);

        try {
            queryUseCase.query(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("User cannot be null", e.getMessage());
        }
    }
}
