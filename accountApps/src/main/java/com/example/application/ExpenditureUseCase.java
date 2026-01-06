package com.example.application;

import java.time.LocalDate;

import com.example.application.command.ConsumptionCommand;
import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.service.ConsumptionService;
import com.example.domain.valueobject.PaywaySupport;

public class ExpenditureUseCase {
    private ExpenditureRecordRepository expenditureRecordRepository;

    private ConsumptionService consumptionService;

    public ExpenditureUseCase(ConsumptionService consumptionService,
        ExpenditureRecordRepository expenditureRecordRepository){
            this.consumptionService = consumptionService;
            this.expenditureRecordRepository = expenditureRecordRepository;
    }

    public void execute(ExpenditureCommand command){
        
        validateInput(command);
    
        consumptionService.execute(command);
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
