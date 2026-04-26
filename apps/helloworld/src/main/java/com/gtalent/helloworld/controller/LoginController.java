package com.gtalent.helloworld.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 處理登入頁面的顯示。
 * 注意：POST /login 的實際驗證邏輯由 Spring Security 框架自動處理，
 * 不需要手動撰寫 @PostMapping。
 */
@Controller
public class LoginController {

    /**
     * 顯示登入頁面。
     * 若使用者已登入（Session 中已有驗證資料），直接轉址到 /hello。
     */
    @GetMapping("/login")
    public String loginPage() {
        // 從 Security Context 取得目前的驗證狀態
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // isAuthenticated() 為 true 且不是匿名使用者，表示已登入
        if (auth != null && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/hello";
        }

        // 未登入 → 顯示登入頁
        return "login";
    }
}
