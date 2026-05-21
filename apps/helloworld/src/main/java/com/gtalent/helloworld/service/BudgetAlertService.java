package com.gtalent.helloworld.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.BudgetAlertLog;
import com.gtalent.helloworld.domain.model.BudgetSetting;
import com.gtalent.helloworld.domain.valueobject.AlertStatus;
import com.gtalent.helloworld.repository.BudgetAlertLogRepository;
import com.gtalent.helloworld.repository.BudgetSettingRepository;

@Service
public class BudgetAlertService {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertService.class);
    private static final int MAX_DEFERRED_RETRIES = 3;

    private final TaskScheduler taskScheduler;
    private final BudgetSettingRepository budgetSettingRepository;
    private final BudgetAlertLogRepository budgetAlertLogRepository;
    private final BudgetAlertExecutor executor;

    /** userId → 目前排程的 Future，用於取消舊排程 */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public BudgetAlertService(TaskScheduler taskScheduler,
                              BudgetSettingRepository budgetSettingRepository,
                              BudgetAlertLogRepository budgetAlertLogRepository,
                              BudgetAlertExecutor executor) {
        this.taskScheduler = taskScheduler;
        this.budgetSettingRepository = budgetSettingRepository;
        this.budgetAlertLogRepository = budgetAlertLogRepository;
        this.executor = executor;
    }

    // ─── Scheduling ────────────────────────────────────────────────────

    /**
     * 為指定使用者建立（或替換）一個 CronTrigger。
     * cron 格式：秒 分 時 日 月 週，例如 "0 30 21 * * ?" = 每天 21:30
     */
    public void scheduleForUser(BudgetSetting setting) {
        cancelForUser(setting.getUserId());
        if (!setting.isEnabled()) return;

        String cron = String.format("0 %d %d * * ?", setting.getAlertMinute(), setting.getAlertHour());
        CronTrigger trigger = new CronTrigger(cron);
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executor.executeAlert(setting.getUserId()), trigger);
        scheduledTasks.put(setting.getUserId(), future);
        log.info("[預算提醒] 使用者 {} 排程已設定：{}", setting.getUserId(), cron);
    }

    public void cancelForUser(Long userId) {
        ScheduledFuture<?> existing = scheduledTasks.remove(userId);
        if (existing != null) {
            existing.cancel(false);
            log.info("[預算提醒] 使用者 {} 舊排程已取消", userId);
        }
    }

    // ─── Deferred Retry（每 5 分鐘）────────────────────────────────────

    @Scheduled(fixedDelay = 300_000)
    public void retryFailed() {
        List<BudgetAlertLog> failedLogs = budgetAlertLogRepository
                .findByStatusAndRetryCountLessThan(AlertStatus.FAILED, MAX_DEFERRED_RETRIES);
        if (failedLogs.isEmpty()) return;

        log.info("[預算提醒] 延遲重試：共 {} 筆 FAILED log", failedLogs.size());
        for (BudgetAlertLog failedLog : failedLogs) {
            try {
                executor.retryAlertLog(failedLog);
            } catch (Exception e) {
                failedLog.setRetryCount(failedLog.getRetryCount() + 1);
                failedLog.setLastAttemptAt(LocalDateTime.now());
                budgetAlertLogRepository.save(failedLog);
                log.warn("[預算提醒] 延遲重試失敗（第 {} 次）：使用者 {} - {}",
                        failedLog.getRetryCount(), failedLog.getUserId(), e.getMessage());
            }
        }
    }

    // ─── Persistence + Misfire on startup ──────────────────────────────

    /**
     * 應用啟動後：
     * 1. 重載所有 enabled 排程（Persistence）
     * 2. 比對今天提醒時間是否已錯過但尚未提醒（Misfire）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reloadOnStartup() {
        List<BudgetSetting> settings = budgetSettingRepository.findAllByEnabledTrue();
        log.info("[預算提醒] 啟動重載 {} 筆排程設定", settings.size());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (BudgetSetting setting : settings) {
            scheduleForUser(setting);

            // Misfire：今天提醒時間已過，但尚未有提醒記錄
            LocalTime alertTime = LocalTime.of(setting.getAlertHour(), setting.getAlertMinute());
            if (now.isAfter(alertTime) && !budgetAlertLogRepository.existsByUserIdAndDate(setting.getUserId(), today)) {
                log.info("[預算提醒] 使用者 {} 偵測到 misfire（排程時間 {}），立即補執行", setting.getUserId(), alertTime);
                executor.executeAlert(setting.getUserId());
            }
        }
    }

    // ─── Settings CRUD ──────────────────────────────────────────────────

    @Transactional
    public BudgetSetting saveSetting(Long userId, int threshold, int alertHour, int alertMinute, boolean enabled) {
        BudgetSetting setting = budgetSettingRepository.findByUserId(userId)
                .orElse(new BudgetSetting(userId, threshold, alertHour, alertMinute, enabled));
        setting.setThreshold(threshold);
        setting.setAlertHour(alertHour);
        setting.setAlertMinute(alertMinute);
        setting.setEnabled(enabled);
        BudgetSetting saved = budgetSettingRepository.save(setting);
        scheduleForUser(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public BudgetSetting getOrDefault(Long userId) {
        return budgetSettingRepository.findByUserId(userId)
                .orElse(new BudgetSetting(userId, 3000, 21, 0, false));
    }
}
