package com.example.domain.service;

import com.example.application.command.ConsumptionCommand;
import com.example.application.command.ExpenditureCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.valueobject.PaymentMethod;
import com.example.domain.valueobject.UserIdentity;

/**
 * Domain service responsible for recording a consumption into expenditure records.
 */
public class ConsumptionService {

    private final ExpenditureRecordRepository expenditureRecordRepository;

    public ConsumptionService(ExpenditureRecordRepository expenditureRecordRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    public void execute(ExpenditureCommand command) {
        
        // 將 User 轉換為 UserIdentity
        UserIdentity userIdentity = UserIdentity.from(command.getUser());
        
        ExpenditureRecord expenditureRecord = new ExpenditureRecord(
            userIdentity,
            command.getName(),
            command.getMoney(),
            command.getCategory(),
            PaymentMethod.valueOf(command.getPayway())
        );

        expenditureRecordRepository.save(expenditureRecord);
    }
}
