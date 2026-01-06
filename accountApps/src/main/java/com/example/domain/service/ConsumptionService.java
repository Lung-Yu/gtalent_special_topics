package com.example.domain.service;

import com.example.application.command.ConsumptionCommand;
import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.repository.ExpenditureRecordRepository;

/**
 * Domain service responsible for recording a consumption into expenditure records.
 */
public class ConsumptionService {

    private final ExpenditureRecordRepository expenditureRecordRepository;

    public ConsumptionService(ExpenditureRecordRepository expenditureRecordRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    public void execute(ExpenditureCommand command) {
        
        ExpenditureRecord expenditureRecord = new ExpenditureRecord(
            command.getUser(),
            command.getName(),
            command.getMoney(),
            command.getCategory(),
            command.getPayway()
        );

        expenditureRecordRepository.save(expenditureRecord);
    }
}
