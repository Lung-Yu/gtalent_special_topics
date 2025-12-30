package com.example.domain.service;

import java.util.List;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;

public interface StatisticsCalculator {
    List<StatisticsPoint> calculate(List<ExpenditureRecord> expenditureRecords);
}
