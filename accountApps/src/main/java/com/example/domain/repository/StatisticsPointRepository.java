package com.example.domain.repository;

import java.util.List;

import com.example.domain.model.StatisticsPoint;

public interface StatisticsPointRepository {
    void saveAll(List<StatisticsPoint> statisticsPoints);
    List<StatisticsPoint> findAll();
}
