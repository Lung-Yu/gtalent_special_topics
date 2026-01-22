package com.example.presentation;

import com.example.domain.model.User;
import java.util.Scanner;

/**
 * 登入控制器
 * 處理使用者登入邏輯
 */
public class LoginController {
    private final Scanner scanner;
    private final LoginView view;
    
    // 預設的帳號密碼
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final int MAX_ATTEMPTS = 3;
    
    public LoginController(Scanner scanner) {
        this.scanner = scanner;
        this.view = new LoginView();
    }
    
    /**
     * 執行登入流程
     * @return 登入成功返回 User 物件，失敗返回 null
     */
    public User login() {
        view.showLoginHeader();
        
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            try {
                // 提示輸入帳號
                view.promptUsername();
                String username = scanner.nextLine().trim();
                
                // 提示輸入密碼
                view.promptPassword();
                String password = scanner.nextLine().trim();
                
                // 驗證帳號密碼
                if (authenticate(username, password)) {
                    view.showLoginSuccess(username);
                    return new User(username);
                } else {
                    attempts++;
                    int remainingAttempts = MAX_ATTEMPTS - attempts;
                    view.showLoginFailed(remainingAttempts);
                }
                
            } catch (Exception e) {
                view.showErrorMessage("登入過程發生錯誤：" + e.getMessage());
                attempts++;
            }
        }
        
        // 超過嘗試次數
        return null;
    }
    
    /**
     * 驗證帳號密碼
     * @param username 使用者帳號
     * @param password 使用者密碼
     * @return 驗證成功返回 true，失敗返回 false
     */
    private boolean authenticate(String username, String password) {
        return DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password);
    }
}
