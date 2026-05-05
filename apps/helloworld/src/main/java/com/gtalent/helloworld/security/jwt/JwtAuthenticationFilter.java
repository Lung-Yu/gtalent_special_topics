package com.gtalent.helloworld.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 驗證 Filter：每個 request 執行一次（OncePerRequestFilter）。
 *
 * 流程：
 *   1. 從 Authorization header 取出 Bearer token
 *   2. 用 JwtUtil 驗證 token 有效性
 *   3. 有效 → 解出 username，建立 Authentication 物件並寫入 SecurityContext
 *   4. 無效或未帶 token → 不設定 Authentication（由後續 Security 規則決定是否拒絕）
 *
 * 此 filter 僅在 JWT FilterChain 中被掛載（JwtSecurityConfig），
 * Session mode 下不會載入。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 優先從 Authorization: Bearer header 取 token，
     * 其次從 HttpOnly Cookie "jwt" 取（瀏覽器登入用）。
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // 從 Cookie 取（瀏覽器自動帶）
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
