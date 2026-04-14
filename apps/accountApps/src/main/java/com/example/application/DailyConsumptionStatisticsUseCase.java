package com.example.application;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.StatisticsCalculator;
import com.example.domain.service.StatisticsStrategy;
import com.example.domain.service.StatisticsStrategyFactory;

import java.util.List;

public class DailyConsumptionStatisticsUseCase {
    private StatisticsPointRepository statisticsPointRepository;
    private StatisticsStrategyFactory strategyFactory;
    
    public DailyConsumptionStatisticsUseCase(
            ExpenditureRecordRepository expenditureRecordRepository,
            StatisticsPointRepository statisticsPointRepository,
            UserRepository userRepository,
            StatisticsCalculator statisticsCalculator) {
        this.statisticsPointRepository = statisticsPointRepository;
        this.strategyFactory = new StatisticsStrategyFactory(
            userRepository, expenditureRecordRepository, statisticsPointRepository
        );
    }
    
    public void calculate(DailyStatisticsCommand command) {
        // 使用工廠創建適當的統計策略
        StatisticsStrategy strategy = strategyFactory.create(command);
        
        // 執行統計策略
        List<StatisticsPoint> points = strategy.execute(command);
        
        // 儲存結果
        statisticsPointRepository.saveAll(points);
    }
}
