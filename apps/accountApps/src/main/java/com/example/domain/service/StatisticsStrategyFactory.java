package com.example.domain.service;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.strategy.ManagerStatisticsStrategy;
import com.example.domain.service.strategy.UserStatisticsStrategy;
import com.example.domain.valueobject.StatisticsType;

public class StatisticsStrategyFactory {
    private UserRepository userRepository;
    private ExpenditureRecordRepository expenditureRecordRepository;
    private StatisticsPointRepository statisticsPointRepository;
    
    public StatisticsStrategyFactory(UserRepository userRepository,
                                   ExpenditureRecordRepository expenditureRecordRepository,
                                   StatisticsPointRepository statisticsPointRepository) {
        this.userRepository = userRepository;
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.statisticsPointRepository = statisticsPointRepository;
    }
    
    public StatisticsStrategy create(DailyStatisticsCommand command) {
        StatisticsType type = command.getStatisticsType();
        
        switch (type) {
            case USER_STATISTICS:
                return new UserStatisticsStrategy(userRepository, expenditureRecordRepository, 
                                                 statisticsPointRepository);
            case MANAGER_STATISTICS:
                return new ManagerStatisticsStrategy(userRepository, expenditureRecordRepository, 
                                                    statisticsPointRepository);
            case DEPARTMENT_STATISTICS:
                // TODO: 將來實作 DepartmentStatisticsStrategy
                throw new UnsupportedOperationException("部門統計功能尚未實作");
            case PERIOD_STATISTICS:
                // TODO: 將來實作 PeriodStatisticsStrategy
                throw new UnsupportedOperationException("時間區間統計功能尚未實作");
            default:
                throw new IllegalArgumentException("不支援的統計類型: " + type);
        }
    }
}