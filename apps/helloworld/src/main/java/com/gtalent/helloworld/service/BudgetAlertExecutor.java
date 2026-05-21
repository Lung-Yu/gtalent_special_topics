package com.gtalent.helloworld.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.BudgetAlertLog;
import com.gtalent.helloworld.domain.model.BudgetSetting;
import com.gtalent.helloworld.domain.valueobject.AlertStatus;
import com.gtalent.helloworld.repository.BudgetAlertLogRepository;
import com.gtalent.helloworld.repository.BudgetSettingRepository;
import com.gtalent.helloworld.repository.ExpenditureRecordRepository;

/**
 * 負責執行單次預算提醒邏輯，獨立為 bean 以確保 @Retryable AOP 正確套用。
 */
@Service
public class BudgetAlertExecutor {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertExecutor.class);

    private final BudgetSettingRepository budgetSettingRepository;
    private final BudgetAlertLogRepository budgetAlertLogRepository;
    private final ExpenditureRecordRepository expenditureRecordRepository;

    public BudgetAlertExecutor(BudgetSettingRepository budgetSettingRepository,
                               BudgetAlertLogRepository budgetAlertLogRepository,
                               ExpenditureRecordRepository expenditureRecordRepository) {
        this.budgetSettingRepository = budgetSettingRepository;
        this.budgetAlertLogRepository = budgetAlertLogRepository;
        this.expenditureRecordRepository = expenditureRecordRepository;
    }

    /**
     * 執行預算提醒：計算今日消費，超標則寫入 alert log。
     * 失敗時最多立即重試 3 次（間隔 2 秒）。
     */
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional
    public void executeAlert(Long userId) {
        LocalDate today = LocalDate.now();

        // 冪等保護：今天已有任何狀態的 log 則跳過
        if (budgetAlertLogRepository.existsByUserIdAndDate(userId, today)) {
            return;
        }

        BudgetSetting setting = budgetSettingRepository.findByUserId(userId).orElse(null);
        if (setting == null || !setting.isEnabled()) return;

        int totalSpent = expenditureRecordRepository.sumMoneyByUserIdAndDate(userId, today);
        if (totalSpent >= setting.getThreshold()) {
            budgetAlertLogRepository.save(new BudgetAlertLog(userId, today, totalSpent, AlertStatus.SENT));
            log.info("[預算提醒] 使用者 {} 今日消費 {} 超過預算 {}，已寫入提醒", userId, totalSpent, setting.getThreshold());
        }
    }

    /**
     * 3 次即時重試全部失敗後的補救：寫入 FAILED log 供延遲重試排程處理。
     */
    @Recover
    @Transactional
    public void recoverAlert(Exception ex, Long userId) {
        LocalDate today = LocalDate.now();
        log.error("[預算提醒] 使用者 {} 即時重試 3 次仍失敗，標記 FAILED：{}", userId, ex.getMessage());
        if (!budgetAlertLogRepository.existsByUserIdAndDate(userId, today)) {
            budgetAlertLogRepository.save(new BudgetAlertLog(userId, today, 0, AlertStatus.FAILED));
        }
    }

    /**
     * 延遲重試用：重新計算並嘗試更新 FAILED log 為 SENT。
     */
    @Transactional
    public void retryAlertLog(BudgetAlertLog failedLog) {
        BudgetSetting setting = budgetSettingRepository.findByUserId(failedLog.getUserId()).orElse(null);
        if (setting == null) return;

        int totalSpent = expenditureRecordRepository
                .sumMoneyByUserIdAndDate(failedLog.getUserId(), failedLog.getDate());
        failedLog.setStatus(AlertStatus.SENT);
        failedLog.setLastAttemptAt(LocalDateTime.now());
        budgetAlertLogRepository.save(failedLog);
        log.info("[預算提醒] 延遲重試成功：使用者 {}，當日消費 {}", failedLog.getUserId(), totalSpent);
    }
}
