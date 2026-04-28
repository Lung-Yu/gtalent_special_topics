package com.gtalent.helloworld.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具類別：負責產生、驗證 JWT token，以及從 token 取出使用者名稱。
 *
 * 使用 JJWT 0.12.x API（Jwts.builder() / Jwts.parser()）。
 * Secret 與有效秒數均由 application.properties 注入，避免硬編碼。
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationSeconds) {
        // HMAC-SHA256 需要 ≥ 256 bits（32 bytes）的 key
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * 依 username 產生 JWT token。
     *
     * @param username 登入成功的使用者名稱
     * @return 簽名後的 JWT 字串
     */
    public String generateToken(String username) {
        long nowMillis = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(nowMillis))
                .expiration(new Date(nowMillis + expirationSeconds * 1000))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 從 token 中取出 username（JWT subject）。
     *
     * @param token JWT 字串
     * @return username
     * @throws JwtException token 無效或已過期時拋出
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 驗證 token 是否有效（簽名正確且未過期）。
     *
     * @param token JWT 字串
     * @return true 代表有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
