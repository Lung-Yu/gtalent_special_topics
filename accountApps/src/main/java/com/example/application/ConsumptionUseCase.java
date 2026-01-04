package com.example.application;

import com.example.application.command.ConsumptionCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.repository.ExpenditureRecordRepository;

public class ConsumptionUseCase {
    
    private ExpenditureRecordRepository expenditureRecordRepository;

    public ConsumptionUseCase(ExpenditureRecordRepository expenditureRecordRepository){
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    public void execute(ConsumptionCommand command) {
        
        ExpenditureRecord expenditureRecord = new ExpenditureRecord(
            command.getUser(),
            command.getName(),
            command.getMoney(),
            command.getCategory(),
            command.getDate()
        );

        expenditureRecordRepository.save(expenditureRecord);
    }
}
