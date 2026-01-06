package com.example.application;

import java.util.List;

import com.example.application.command.ExpenditureQueryCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.repository.ExpenditureRecordRepository;

public class ExpenditureQueryUseCase {

    private final ExpenditureRecordRepository expenditureRecordRepository;

    public ExpenditureQueryUseCase(ExpenditureRecordRepository expenditureRecordRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    public List<ExpenditureRecord> query(ExpenditureQueryCommand command) {
        validate(command);

        if (command.getDate() != null) {
            return expenditureRecordRepository.findByUserAndDate(command.getUser(), command.getDate());
        }
        return expenditureRecordRepository.findByUser(command.getUser());
    }

    private void validate(ExpenditureQueryCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.getUser() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }
}
