package com.example.presentation;

/**
 * 選單選項枚舉
 * 定義系統所有可用的選單選項
 */
public enum MenuOption {
    EXIT(0, "退出系統"),
    EXPENDITURE(1, "支出功能"),
    CATEGORY_MANAGEMENT(2, "分類標籤管理"),
    QUERY_RECORDS(3, "消費紀錄查詢");
    
    private final int code;
    private final String description;
    
    MenuOption(int code, String description) {
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
     * @param code 選項代碼
     * @return 對應的 MenuOption，如果無效則返回 null
     */
    public static MenuOption fromCode(int code) {
        for (MenuOption option : values()) {
            if (option.code == code) {
                return option;
            }
        }
        return null;
    }
    
    /**
     * 檢查代碼是否有效
     * @param code 選項代碼
     * @return 如果代碼有效返回 true
     */
    public static boolean isValid(int code) {
        return fromCode(code) != null;
    }
}
