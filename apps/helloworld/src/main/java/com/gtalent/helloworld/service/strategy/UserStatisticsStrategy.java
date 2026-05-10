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
 * 使用者統計策略：按「使用者 + 分類」聚合支出金額。
 */
@Component
public class UserStatisticsStrategy implements StatisticsStrategy {

    private final ExpenditureRecordRepository expenditureRecordRepository;

    public UserStatisticsStrategy(ExpenditureRecordRepository expenditureRecordRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    @Override
    public String getType() {
        return "USER";
    }

    @Override
    public List<StatisticsPoint> calculate(LocalDate date) {
        List<ExpenditureRecord> records = expenditureRecordRepository.findByDate(date);

        // key = userId_categoryName
        Map<String, StatisticsPoint> pointMap = new HashMap<>();

        for (ExpenditureRecord expenditureRecord : records) {
            for (Category category : expenditureRecord.getCategories()) {
                String key = expenditureRecord.getUser().getId() + "_" + category.getName();

                pointMap.merge(
                    key,
                    new StatisticsPoint(
                        expenditureRecord.getUser().getId(),
                        expenditureRecord.getUser().getUsername(),
                        expenditureRecord.getMoney(),
                        category.getName(),
                        date
                    ),
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
