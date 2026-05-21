package com.gtalent.helloworld.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "budget_settings")
public class BudgetSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int threshold;

    @Column(nullable = false)
    private int alertHour;

    @Column(nullable = false)
    private int alertMinute;

    @Column(nullable = false)
    private boolean enabled = true;

    protected BudgetSetting() {}

    public BudgetSetting(Long userId, int threshold, int alertHour, int alertMinute, boolean enabled) {
        this.userId = userId;
        this.threshold = threshold;
        this.alertHour = alertHour;
        this.alertMinute = alertMinute;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }

    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }

    public int getAlertHour() { return alertHour; }
    public void setAlertHour(int alertHour) { this.alertHour = alertHour; }

    public int getAlertMinute() { return alertMinute; }
    public void setAlertMinute(int alertMinute) { this.alertMinute = alertMinute; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
