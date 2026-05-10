package com.gtalent.helloworld.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.StatisticsPoint;
import com.gtalent.helloworld.repository.StatisticsPointRepository;
import com.gtalent.helloworld.service.strategy.StatisticsStrategy;

@Service
@Transactional
public class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);

    private final StatisticsPointRepository statisticsPointRepository;

    /** 所有 StatisticsStrategy bean 自動收集 */
    private final Map<String, StatisticsStrategy> strategyMap;

    public StatisticsService(StatisticsPointRepository statisticsPointRepository,
                             List<StatisticsStrategy> strategies) {
        this.statisticsPointRepository = statisticsPointRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(StatisticsStrategy::getType, Function.identity()));
    }

    /**
     * 核心計算邏輯：根據類型和日期計算並儲存統計點。
     * 手動觸發（Controller）與排程觸發（@Scheduled）共用此方法。
     *
     * @param type 策略類型（USER / MANAGER）
     * @param date 統計日期
     * @return 儲存後的統計點清單
     */
    public List<StatisticsPoint> calculateAndSave(String type, LocalDate date) {
        StatisticsStrategy strategy = strategyMap.get(type.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("不支援的統計類型: " + type
                    + "，可用類型：" + strategyMap.keySet());
        }
        List<StatisticsPoint> points = strategy.calculate(date);
        return statisticsPointRepository.saveAll(points);
    }

    /**
     * 每日午夜自動觸發，對「前一天」執行 USER 與 MANAGER 兩種統計。
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledDailyCalculation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[統計排程] 開始計算 {} 的統計資料", yesterday);
        calculateAndSave("USER", yesterday);
        calculateAndSave("MANAGER", yesterday);
        log.info("[統計排程] 完成計算 {} 的統計資料", yesterday);
    }

    @Transactional(readOnly = true)
    public List<StatisticsPoint> findAll() {
        return statisticsPointRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StatisticsPoint> findByDate(LocalDate date) {
        return statisticsPointRepository.findByDate(date);
    }
}
