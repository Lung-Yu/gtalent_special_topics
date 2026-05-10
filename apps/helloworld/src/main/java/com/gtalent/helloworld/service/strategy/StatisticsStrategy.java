package com.gtalent.helloworld.service.strategy;

import java.time.LocalDate;
import java.util.List;

import com.gtalent.helloworld.domain.model.StatisticsPoint;

public interface StatisticsStrategy {

    /** 策略類型識別名稱，用於 StatisticsService 選擇策略 */
    String getType();

    /**
     * 計算指定日期的統計點
     *
     * @param date 統計日期
     * @return 計算結果的統計點清單
     */
    List<StatisticsPoint> calculate(LocalDate date);
}
