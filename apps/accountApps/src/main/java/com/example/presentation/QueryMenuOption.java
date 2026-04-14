package com.example.presentation;

/**
 * 消費紀錄查詢選單選項枚舉
 */
public enum QueryMenuOption {
    BACK(0, "返回主選單"),
    VIEW_ALL(1, "查看所有記錄"),
    VIEW_BY_DATE(2, "按日期查詢");
    
    private final int code;
    private final String description;
    
    QueryMenuOption(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根據代碼獲取選單選項
     */
    public static QueryMenuOption fromCode(int code) {
        for (QueryMenuOption option : values()) {
            if (option.code == code) {
                return option;
            }
        }
        return null;
    }
}
