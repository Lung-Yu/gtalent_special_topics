package com.example.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;

import com.example.domain.model.StatisticsPoint;
import com.example.domain.repository.StatisticsPointRepository;

public class InMemoryStatisticsPointRepository implements StatisticsPointRepository {

    private List<StatisticsPoint> statisticsPoints = new ArrayList<>();

    @Override
    public void saveAll(List<StatisticsPoint> statisticsPoints) {
        this.statisticsPoints.addAll(statisticsPoints);
    }

    @Override
    public List<StatisticsPoint> findAll() {
        return new ArrayList<>(statisticsPoints);
    }
}
