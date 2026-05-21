package com.gtalent.helloworld.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.BudgetAlertLog;
import com.gtalent.helloworld.domain.valueobject.AlertStatus;

public interface BudgetAlertLogRepository extends JpaRepository<BudgetAlertLog, Long> {

    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    Optional<BudgetAlertLog> findByUserIdAndDate(Long userId, LocalDate date);

    List<BudgetAlertLog> findByStatusAndRetryCountLessThan(AlertStatus status, int maxRetryCount);
}
