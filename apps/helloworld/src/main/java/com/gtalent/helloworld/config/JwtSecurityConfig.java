package com.gtalent.helloworld.config;

import com.gtalent.helloworld.security.jwt.JwtAuthenticationFilter;
import com.gtalent.helloworld.security.jwt.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
public class JwtSecurityConfig {

    private final JwtUtil jwtUtil;

    public JwtSecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Stateless：不建立也不使用 HttpSession ──────────────
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── 停用 CSRF（REST API 使用 token，不需要 CSRF 保護）──
            .csrf(csrf -> csrf.disable())

            // ── 存取控制 ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // 登入端點公開（取 token 用）
                .requestMatchers("/auth/login").permitAll()
                // REST API 端點公開（無需 JWT）
                .requestMatchers("/v1/**", "/v2/**").permitAll()
                // 其餘所有路徑需要有效 JWT
                .anyRequest().authenticated()
            )

            // ── 掛載 JWT Filter ────────────────────────────────────
            // 在 Spring Security 的帳密驗證 filter 之前執行，
            // 讓 SecurityContext 在後續 filter 中已有 Authentication 物件
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
