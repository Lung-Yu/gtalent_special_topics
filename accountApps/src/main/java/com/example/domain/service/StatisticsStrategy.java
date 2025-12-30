package com.example.domain.service;

import java.util.List;

import com.example.application.command.DailyStatisticsCommand;
import com.example.domain.model.StatisticsPoint;

public interface StatisticsStrategy {
    List<StatisticsPoint> execute(DailyStatisticsCommand command);
}