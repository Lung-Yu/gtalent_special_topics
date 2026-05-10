package com.gtalent.helloworld.service.strategy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.domain.model.StatisticsPoint;
import com.gtalent.helloworld.repository.ExpenditureRecordRepository;

/**
 * 管理者統計策略：只按「分類」聚合，不區分使用者（userId = null）。
 */
@Component
public class ManagerStatisticsStrategy implements StatisticsStrategy {

    private final ExpenditureRecordRepository expenditureRecordRepository;

    public ManagerStatisticsStrategy(ExpenditureRecordRepository expenditureRecordRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    @Override
    public String getType() {
        return "MANAGER";
    }

    @Override
    public List<StatisticsPoint> calculate(LocalDate date) {
        List<ExpenditureRecord> records = expenditureRecordRepository.findByDate(date);

        // key = categoryName only (no user distinction)
        Map<String, StatisticsPoint> pointMap = new HashMap<>();

        for (ExpenditureRecord expenditureRecord : records) {
            for (Category category : expenditureRecord.getCategories()) {
                String key = category.getName();

                pointMap.merge(
                    key,
                    new StatisticsPoint(null, null, expenditureRecord.getMoney(), category.getName(), date),
                    (existing, newPoint) -> {
                        existing.setAmount(existing.getAmount() + newPoint.getAmount());
                        return existing;
                    }
                );
            }
        }

        return new ArrayList<>(pointMap.values());
    }
}
