package com.example.domain.model;

import java.time.LocalDateTime;

import com.example.domain.valueobject.StatisticsCategory;
import com.example.domain.valueobject.UserIdentity;

public class StatisticsPoint {
    private int amount;
    private UserIdentity userIdentity;
    private LocalDateTime time;
    private StatisticsCategory category;
    
    public StatisticsPoint() {
    }
    
    /**
     * 使用 UserIdentity 建立統計點
     * 適用於統計場景，避免載入不必要的使用者資料
     * 
     * @param amount 金額
     * @param userIdentity 使用者身份（可為 null 表示聚合統計）
     * @param time 時間
     * @param category 分類
     */
    public StatisticsPoint(int amount, UserIdentity userIdentity, LocalDateTime time, StatisticsCategory category) {
        this.amount = amount;
        this.userIdentity = userIdentity;
        this.time = time;
        this.category = category;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public int getAmount() {
        return amount;
    }
    
    /**
     * 取得使用者身份識別
     * 
     * @return UserIdentity，可能為 null（表示聚合統計）
     */
    public UserIdentity getUserIdentity() {
        return userIdentity;
    }
    
    /**
     * 取得使用者名稱（便利方法）
     * 
     * @return 使用者名稱，如果是聚合統計則返回 null
     */
    public String getUsername() {
        return userIdentity != null ? userIdentity.getUsername() : null;
    }
    
    /**
     * 判斷是否為聚合統計（不區分使用者）
     * 
     * @return true 如果是聚合統計
     */
    public boolean isAggregated() {
        return userIdentity == null;
    }
    
    /**
     * 判斷是否為特定使用者的統計
     * 
     * @param username 使用者名稱
     * @return true 如果是該使用者的統計
     */
    public boolean isForUser(String username) {
        String thisUsername = getUsername();
        return thisUsername != null && thisUsername.equals(username);
    }
    
    public LocalDateTime getTime() {
        return time;
    }
    
    public void setTime(LocalDateTime time) {
        this.time = time;
    }
    
    public StatisticsCategory getCategory() {
        return category;
    }
    
    public void setCategory(StatisticsCategory category) {
        this.category = category;
    }
}
