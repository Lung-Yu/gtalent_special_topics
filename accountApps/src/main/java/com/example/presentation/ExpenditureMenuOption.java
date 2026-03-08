package com.example.presentation;

/**
 * 支出功能選單選項枚舉
 */
public enum ExpenditureMenuOption {
    BACK(0, "返回主選單"),
    CREATE(1, "新增支出記錄"),
    VIEW_TODAY(2, "查看今日支出"),
    VIEW_RECENT(3, "查看最近支出記錄（分頁）");
    
    private final int code;
    private final String description;
    
    ExpenditureMenuOption(int code, String description) {
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
    public static ExpenditureMenuOption fromCode(int code) {
        for (ExpenditureMenuOption option : values()) {
            if (option.code == code) {
                return option;
            }
        }
        return null;
    }
}
