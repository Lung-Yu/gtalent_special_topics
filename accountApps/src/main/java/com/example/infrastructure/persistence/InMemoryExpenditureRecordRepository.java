package com.example.infrastructure.persistence;

import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryExpenditureRecordRepository implements ExpenditureRecordRepository {
    private List<ExpenditureRecord> records = new ArrayList<>();
    
    @Override
    public List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date) {
        return records.stream()
                .filter(record -> record.getUser().equals(user) && 
                         record.getDate().equals(date))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExpenditureRecord> findByDate(LocalDate date) {
        return records.stream()
                .filter(record -> record.getDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenditureRecord> findByUser(User user) {
        return records.stream()
                .filter(record -> record.getUser().equals(user))
                .collect(Collectors.toList());
    }
    
    @Override
    public void save(ExpenditureRecord record) {
        records.add(record);
    }
    
    @Override
    public List<ExpenditureRecord> findAll() {
        return new ArrayList<>(records);
    }
}
