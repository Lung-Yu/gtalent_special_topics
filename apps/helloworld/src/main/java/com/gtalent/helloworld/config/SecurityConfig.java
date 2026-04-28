package com.gtalent.helloworld.config;

import com.gtalent.helloworld.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Spring Security 核心設定：
 * - 定義哪些路徑需要驗證
 * - 設定登入/登出行為
 * - 設定 BCrypt 密碼編碼器
 * - 設定 Session 管理（Session Fixation Protection）
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * BCrypt 密碼編碼器 bean，
     * 用於 createUser 時加密密碼，以及 Spring Security 驗證時比對密碼。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 將 UserDetailsService 與 PasswordEncoder 組合為 DaoAuthenticationProvider，
     * Spring Security 驗證時會呼叫此 provider。
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 暴露 AuthenticationManager bean，可在其他地方注入（如測試用途）。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 監聽 HttpSession 生命週期，讓 Spring Security 的 Session 並發控制正常運作。
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * Security Filter Chain：定義存取規則、登入登出設定、Session 管理。
     *
     * 僅在 app.auth.mode=session（預設）時載入。
     * JWT mode 下此 chain 不會被建立，由 JwtSecurityConfig 接管。
     */
    @Bean
    @Order(2)
    @ConditionalOnProperty(name = "app.auth.mode", havingValue = "session", matchIfMissing = true)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())

            // ── 存取控制 ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // 靜態資源與登入頁公開
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // REST API 端點公開（保持 Postman 可用）
                .requestMatchers("/v1/**", "/v2/**").permitAll()
                // 其餘所有路徑需要登入
                .anyRequest().authenticated()
            )

            // ── 登入設定 ──────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")               // 自訂登入頁 GET
                .loginProcessingUrl("/login")       // 表單 POST 目標（框架自動處理）
                .usernameParameter("username")      // 表單欄位名稱
                .passwordParameter("password")      // 表單欄位名稱
                .defaultSuccessUrl("/hello", true)  // 登入成功轉址
                .failureUrl("/login?error")         // 登入失敗轉址
                .permitAll()
            )

            // ── 登出設定 ──────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")               // POST /logout 觸發登出
                .invalidateHttpSession(true)        // 清除 Session
                .clearAuthentication(true)          // 清除 Security Context
                .deleteCookies("JSESSIONID")        // 刪除 Session Cookie
                .logoutSuccessUrl("/login?logout")  // 登出成功轉址
                .permitAll()
            );

            // // ── Session 管理 ──────────────────────────────────────
            // .sessionManagement(session -> session
            //     // Session Fixation Protection：登入後自動建立新 Session
            //     .sessionFixation().migrateSession()
            //     // 同一帳號最多 1 個並發 Session
            //     .maximumSessions(1)
            //     .expiredUrl("/login?expired")
            // );

        return http.build();
    }
}
