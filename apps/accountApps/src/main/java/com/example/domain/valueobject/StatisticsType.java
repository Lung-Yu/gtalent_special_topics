package com.example.domain.valueobject;

public enum StatisticsType {
    USER_STATISTICS(1, "使用者統計"),
    MANAGER_STATISTICS(2, "管理者統計"),
    DEPARTMENT_STATISTICS(3, "部門統計"),
    PERIOD_STATISTICS(4, "時間區間統計");
    
    private final int code;
    private final String description;
    
    StatisticsType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static StatisticsType fromCode(int code) {
        for (StatisticsType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown statistics type code: " + code);
    }
}