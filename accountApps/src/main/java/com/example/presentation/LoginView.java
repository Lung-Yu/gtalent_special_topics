package com.example.presentation;

/**
 * 登入視圖
 * 負責顯示登入相關的介面
 */
public class LoginView {
    
    /**
     * 顯示登入畫面標題
     */
    public void showLoginHeader() {
        System.out.println("\n========================================");
        System.out.println("        歡迎使用記帳系統");
        System.out.println("========================================");
        System.out.println("請登入以繼續\n");
    }
    
    /**
     * 提示輸入帳號
     */
    public void promptUsername() {
        System.out.print("帳號: ");
    }
    
    /**
     * 提示輸入密碼
     */
    public void promptPassword() {
        System.out.print("密碼: ");
    }
    
    /**
     * 顯示登入成功訊息
     */
    public void showLoginSuccess(String username) {
        System.out.println("\n✓ 登入成功！歡迎，" + username + "\n");
    }
    
    /**
     * 顯示登入失敗訊息
     */
    public void showLoginFailed(int remainingAttempts) {
        System.out.println("\n✗ 登入失敗：帳號或密碼錯誤");
        if (remainingAttempts > 0) {
            System.out.println("剩餘嘗試次數: " + remainingAttempts + "\n");
        } else {
            System.out.println("嘗試次數已達上限，系統即將關閉\n");
        }
    }
    
    /**
     * 顯示錯誤訊息
     */
    public void showErrorMessage(String message) {
        System.out.println("\n✗ 錯誤：" + message + "\n");
    }
    
    /**
     * 顯示一般訊息
     */
    public void showMessage(String message) {
        System.out.println(message);
    }
}
