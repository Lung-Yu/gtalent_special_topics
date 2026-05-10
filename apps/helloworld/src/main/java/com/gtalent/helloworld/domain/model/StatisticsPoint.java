package com.gtalent.helloworld.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "statistics_points")
public class StatisticsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** nullable：null 表示管理者聚合統計（不區分使用者） */
    private Long userId;

    /** denormalized username for display, nullable */
    private String username;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String categoryName;

    /** 統計計算時間 */
    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    /** 統計涵蓋的日期 */
    @Column(nullable = false)
    private LocalDate date;

    protected StatisticsPoint() {}

    public StatisticsPoint(Long userId, String username, int amount,
                           String categoryName, LocalDate date) {
        this.userId = userId;
        this.username = username;
        this.amount = amount;
        this.categoryName = categoryName;
        this.calculatedAt = LocalDateTime.now();
        this.date = date;
    }

    public Long getId() { return id; }

    public Long getUserId() { return userId; }

    public String getUsername() { return username; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCategoryName() { return categoryName; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }

    public LocalDate getDate() { return date; }
}
