package com.example.presentation;

/**
 * 分類管理選單選項枚舉
 */
public enum CategoryMenuOption {
    BACK(0, "返回主選單"),
    CREATE(1, "新增分類標籤"),
    VIEW_ALL(2, "查看所有分類標籤"),
    VIEW_INCOME(3, "查看收入分類"),
    VIEW_OUTCOME(4, "查看支出分類");
    
    private final int code;
    private final String description;
    
    CategoryMenuOption(int code, String description) {
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
    public static CategoryMenuOption fromCode(int code) {
        for (CategoryMenuOption option : values()) {
            if (option.code == code) {
                return option;
            }
        }
        return null;
    }
    
    /**
     * 檢查代碼是否有效
     */
    public static boolean isValid(int code) {
        return fromCode(code) != null;
    }
}
