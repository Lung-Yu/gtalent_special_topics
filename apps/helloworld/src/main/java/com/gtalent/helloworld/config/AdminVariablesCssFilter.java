package com.gtalent.helloworld.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Thymeleaf 3.1.x 禁止 T() 和 new 運算子（安全性限制），
 * 但 Spring Boot Admin 的 variables.css 是 Thymeleaf 模板且使用 T() 注入 CSS 變數，
 * 導致每次 Admin UI 載入 CSS 都拋出 TemplateProcessingException。
 *
 * 此 Filter 在請求進入 DispatcherServlet 之前攔截 variables.css，
 * 直接回傳空白 CSS（不帶任何自訂主題變數），完全繞過 Thymeleaf 處理。
 * Admin UI 會使用 Vue 元件庫的內建預設色彩，功能正常。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminVariablesCssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().endsWith("/variables.css")) {
            response.setContentType("text/css;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(":root {}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
