package com.gtalent.helloworld.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gtalent.helloworld.domain.valueobject.AlertStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "budget_alert_logs")
public class BudgetAlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int totalSpent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false)
    private int retryCount = 0;

    private LocalDateTime lastAttemptAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected BudgetAlertLog() {}

    public BudgetAlertLog(Long userId, LocalDate date, int totalSpent, AlertStatus status) {
        this.userId = userId;
        this.date = date;
        this.totalSpent = totalSpent;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.lastAttemptAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public int getTotalSpent() { return totalSpent; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
