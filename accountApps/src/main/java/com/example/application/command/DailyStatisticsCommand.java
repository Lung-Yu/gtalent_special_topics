package com.example.application.command;

import java.time.LocalDate;

import com.example.domain.valueobject.StatisticsType;

public class DailyStatisticsCommand {
    private LocalDate date;
    private int type;
    
    public DailyStatisticsCommand(LocalDate date) {
        this.date = date;
        this.type = StatisticsType.USER_STATISTICS.getCode(); // 預設為使用者統計
    }
    
    public DailyStatisticsCommand(LocalDate date, int type) {
        this.date = date;
        this.type = type;
    }
    
    public DailyStatisticsCommand(LocalDate date, StatisticsType statisticsType) {
        this.date = date;
        this.type = statisticsType.getCode();
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public StatisticsType getStatisticsType() {
        return StatisticsType.fromCode(type);
    }
    
    public void setStatisticsType(StatisticsType statisticsType) {
        this.type = statisticsType.getCode();
    }
}
