package com.example.domain.service.strategy;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.AdminStatisticsCalculator;
import com.example.domain.service.StatisticsCalculator;
import com.example.domain.service.StatisticsStrategy;

import java.util.List;

public class ManagerStatisticsStrategy implements StatisticsStrategy {
    private ExpenditureRecordRepository expenditureRecordRepository;
    private StatisticsCalculator statisticsCalculator;
    
    public ManagerStatisticsStrategy(UserRepository userRepository,
                                   ExpenditureRecordRepository expenditureRecordRepository,
                                   StatisticsPointRepository statisticsPointRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.statisticsCalculator = new AdminStatisticsCalculator();
    }
    
    @Override
    public List<StatisticsPoint> execute(DailyStatisticsCommand command) {
        List<ExpenditureRecord> allRecords = expenditureRecordRepository.findByDate(command.getDate());
        return statisticsCalculator.calculate(allRecords);
    }
}