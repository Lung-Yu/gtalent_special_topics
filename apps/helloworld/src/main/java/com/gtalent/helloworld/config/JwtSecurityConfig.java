package com.gtalent.helloworld.config;

import com.gtalent.helloworld.security.jwt.JwtAuthenticationFilter;
import com.gtalent.helloworld.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * JWT 認證的 SecurityFilterChain。
 *
 * 僅在 application.properties 設定 app.auth.mode=jwt 時被 Spring 載入。
 * Session mode 下此 config 完全不存在，不影響 Session 行為。
 *
 * 特性：
 *   - 完全 stateless（STATELESS session policy）
 *   - 停用 CSRF（REST API 無需）
 *   - POST /auth/login 公開（讓前端取得 token）
 *   - 其餘所有路徑需要有效 Bearer token
 *   - 掛載 JwtAuthenticationFilter 於 UsernamePasswordAuthenticationFilter 之前
 */
@Configuration
@ConditionalOnProperty(name = "app.auth.mode", havingValue = "jwt")
@Order(1)
public class JwtSecurityConfig {

    private final JwtUtil jwtUtil;

    public JwtSecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
            // ── 停用 form login（Spring Security 預設啟用，會攔截 GET /login 並渲染
            //    Spring Boot Admin 的 login.html，導致 uiSettings=null 錯誤）
            .formLogin(AbstractHttpConfigurer::disable)

            // ── Stateless：不建立也不使用 HttpSession ──────────────
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── 停用 CSRF（REST API 使用 token，不需要 CSRF 保護）──
            .csrf(csrf -> csrf.disable())

            // ── 存取控制 ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // 登入端點公開（帳密驗證 + 2FA 驗證 + 驗證碼刷新）
                .requestMatchers("/auth/login", "/auth/verify", "/auth/refresh-code").permitAll()
                // REST API 端點公開（無需 JWT）
                .requestMatchers("/v1/**", "/v2/**").permitAll()
                // Actuator 端點公開
                .requestMatchers("/actuator/**").permitAll()
                // Spring Boot Admin UI
                .requestMatchers("/admin/**", "/assets/**").permitAll()
                // 靜態資源與登入頁公開（/app-login 是實際登入頁，/login 保留相容）
                .requestMatchers("/app-login", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                // 其餘所有路徑需要有效 JWT
                .anyRequest().authenticated()
            )
            // ── 未認證的瀏覽器請求導向 /app-login ────────────────
            // 注意：不能用 /login，因為 Spring Boot Admin 的 HomepageForwardingFilter
            // 將 /login 列為 SPA 路由並攔截轉發，導致 uiSettings=null 錯誤
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("text/html")) {
                        response.sendRedirect("/app-login");
                    } else {
                        response.sendError(401, "Unauthorized");
                    }
                })
            )
            // ── 掛載 JWT Filter ────────────────────────────────────
            // 在 Spring Security 的帳密驗證 filter 之前執行，
            // 讓 SecurityContext 在後續 filter 中已有 Authentication 物件
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            )

            // ── 登出：清除 HttpOnly Cookie jwt，導向 /login ────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, auth) -> {
                    Cookie cookie = new Cookie("jwt", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                })
                .logoutSuccessUrl("/app-login")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public WebMvcConfigurer loginViewConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // 實際登入頁：/app-login → app-login.html
                // Spring Boot Admin 的 HomepageForwardingFilter 會攔截 /login（已列為 SPA 路由），
                // 所以改用 /app-login 完全避開衝突。
                registry.addViewController("/app-login").setViewName("app-login");
                // /login 保留為相容，重導向到 /app-login
                registry.addRedirectViewController("/login", "/app-login");
            }
        };
    }
}
