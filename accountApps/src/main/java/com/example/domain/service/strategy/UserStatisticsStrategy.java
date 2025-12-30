package com.example.domain.service.strategy;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.StatisticsPointRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.service.DefaultStatisticsCalculator;
import com.example.domain.service.StatisticsCalculator;
import com.example.domain.service.StatisticsStrategy;

import java.util.ArrayList;
import java.util.List;

public class UserStatisticsStrategy implements StatisticsStrategy {
    private UserRepository userRepository;
    private ExpenditureRecordRepository expenditureRecordRepository;
    private StatisticsCalculator statisticsCalculator;
    
    public UserStatisticsStrategy(UserRepository userRepository,
                                ExpenditureRecordRepository expenditureRecordRepository,
                                StatisticsPointRepository statisticsPointRepository) {
        this.userRepository = userRepository;
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.statisticsCalculator = new DefaultStatisticsCalculator();
    }
    
    @Override
    public List<StatisticsPoint> execute(DailyStatisticsCommand command) {
        List<StatisticsPoint> allPoints = new ArrayList<>();
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            List<ExpenditureRecord> records = expenditureRecordRepository.findByUserAndDate(user, command.getDate());
            List<StatisticsPoint> points = statisticsCalculator.calculate(records);
            allPoints.addAll(points);
        }
        
        return allPoints;
    }
}