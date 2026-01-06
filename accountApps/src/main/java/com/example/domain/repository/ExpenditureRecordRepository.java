package com.example.domain.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;

public interface ExpenditureRecordRepository {
    List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date);
    
    List<ExpenditureRecord> findByDate(LocalDate date);

    List<ExpenditureRecord> findByUser(User user);

    void save(ExpenditureRecord record);

    List<ExpenditureRecord> findAll();
}
