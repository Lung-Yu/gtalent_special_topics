package com.example.application;

import com.example.application.command.ConsumptionCommand;
import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.valueobject.PaywaySupport;

public class ExpenditureUseCase {
    private ExpenditureRecordRepository expenditureRecordRepository;

    private ConsumptionUseCase consumptionUseCase;

    public ExpenditureUseCase(ConsumptionUseCase consumptionUseCase,
        ExpenditureRecordRepository expenditureRecordRepository){
            this.consumptionUseCase = consumptionUseCase;
            this.expenditureRecordRepository = expenditureRecordRepository;
    }

    public void execute(ExpenditureCommand command){
        // 1. input validate
        validateInput(command);
        
        // 2. create expenditure record and save
        ExpenditureRecord record = new ExpenditureRecord(
            command.getUser(),
            command.getPayway(),
            command.getMoney(),
            command.getCategory()
        );
        expenditureRecordRepository.save(record);
        
        // 3. call consumptionUsecase for add record
        ConsumptionCommand consumptionCommand = new ConsumptionCommand(
            record.getName(),
            record.getCategory(),
            record.getMoney(),
            record.getUser(),
            record.getDate()
        );
        consumptionUseCase.execute(consumptionCommand);
    }
    
    private void validateInput(ExpenditureCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.getUser() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (command.getMoney() <= 0) {
            throw new IllegalArgumentException("Money must be greater than 0");
        }
        if (command.getPayway() == null || command.getPayway().isEmpty()) {
            throw new IllegalArgumentException("Payway cannot be null or empty");
        }
        if (command.getCategory() == null || command.getCategory().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        
        // Validate payway support
        try {
            PaywaySupport.valueOf(command.getPayway());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported payway: " + command.getPayway());
        }
    }


}
