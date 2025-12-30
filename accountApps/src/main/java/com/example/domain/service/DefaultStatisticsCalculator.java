package com.example.domain.service;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.valueobject.StatisticsCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultStatisticsCalculator implements StatisticsCalculator {
    @Override
    public List<StatisticsPoint> calculate(List<ExpenditureRecord> expenditureRecords) {
        Map<String, StatisticsPoint> pointMap = new HashMap<>();
        
        for (ExpenditureRecord record : expenditureRecords) {
            StatisticsCategory category = "food".equals(record.getCategory()) ? 
                StatisticsCategory.food : StatisticsCategory.salary;
            
            // 建立唯一鍵：使用者名稱 + 類別
            String key = record.getUser().getUsername() + "_" + category.name();
            
            if (pointMap.containsKey(key)) {
                // 如果已存在，累加金額
                StatisticsPoint existingPoint = pointMap.get(key);
                existingPoint.setAmount(existingPoint.getAmount() + record.getMoney());
            } else {
                // 如果不存在，創建新的 StatisticsPoint
                StatisticsPoint point = new StatisticsPoint(
                    record.getMoney(),
                    record.getUser(),
                    LocalDateTime.now(),
                    category
                );
                pointMap.put(key, point);
            }
        }
        
        return new ArrayList<>(pointMap.values());
    }
}
