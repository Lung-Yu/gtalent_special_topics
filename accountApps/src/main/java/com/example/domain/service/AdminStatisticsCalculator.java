package com.example.domain.service;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.valueobject.StatisticsCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理者統計計算器 - 只按類別分組，不分使用者
 */
public class AdminStatisticsCalculator implements StatisticsCalculator {
    @Override
    public List<StatisticsPoint> calculate(List<ExpenditureRecord> expenditureRecords) {
        Map<String, StatisticsPoint> pointMap = new HashMap<>();
        
        for (ExpenditureRecord record : expenditureRecords) {
            // 處理每個分類
            for (String categoryName : record.getCategory()) {
                StatisticsCategory category = "food".equals(categoryName) ? 
                    StatisticsCategory.food : StatisticsCategory.salary;
                
                // 只用類別作為 key，不區分使用者
                String key = category.name();
                
                if (pointMap.containsKey(key)) {
                    // 如果已存在，累加金額
                    StatisticsPoint existingPoint = pointMap.get(key);
                    existingPoint.setAmount(existingPoint.getAmount() + record.getMoney());
                } else {
                    // 如果不存在，創建新的 StatisticsPoint（user 設為 null 表示全體）
                    StatisticsPoint point = new StatisticsPoint(
                        record.getMoney(),
                        null,
                        LocalDateTime.now(),
                        category
                    );
                    pointMap.put(key, point);
                }
            }
        }
        
        return new ArrayList<>(pointMap.values());
    }
}