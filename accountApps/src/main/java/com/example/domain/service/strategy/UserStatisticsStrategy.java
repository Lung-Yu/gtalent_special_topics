package com.example.domain.service.strategy;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.StatisticsStrategy;

import java.util.List;

/**
 * 使用者統計策略
 * 使用資料庫層的聚合查詢，避免 N+1 查詢問題
 */
public class UserStatisticsStrategy implements StatisticsStrategy {
    private ExpenditureRecordRepository expenditureRecordRepository;
    
    public UserStatisticsStrategy(UserRepository userRepository,
                                ExpenditureRecordRepository expenditureRecordRepository,
                                StatisticsPointRepository statisticsPointRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }
    
    @Override
    public List<StatisticsPoint> execute(DailyStatisticsCommand command) {
        // 使用 Repository 層的聚合查詢，一次性取得所有使用者的統計資料
        // 這個方法在資料庫層進行 GROUP BY，避免了原本的 N+1 查詢問題
        return expenditureRecordRepository.findStatisticsByDate(command.getDate());
    }
}