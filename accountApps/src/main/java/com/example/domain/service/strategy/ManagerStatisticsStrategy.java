package com.example.domain.service.strategy;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.StatisticsStrategy;

import java.util.List;

/**
 * 管理者統計策略
 * 使用資料庫層的聚合查詢，只按分類聚合，不區分使用者
 */
public class ManagerStatisticsStrategy implements StatisticsStrategy {
    private ExpenditureRecordRepository expenditureRecordRepository;
    
    public ManagerStatisticsStrategy(UserRepository userRepository,
                                   ExpenditureRecordRepository expenditureRecordRepository,
                                   StatisticsPointRepository statisticsPointRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }
    
    @Override
    public List<StatisticsPoint> execute(DailyStatisticsCommand command) {
        // 使用 Repository 層的聚合查詢，直接在資料庫層按分類聚合
        // 這個方法在資料庫層進行 GROUP BY category，避免了取得所有原始記錄的開銷
        return expenditureRecordRepository.findStatisticsByCategoryAndDate(command.getDate());
    }
}