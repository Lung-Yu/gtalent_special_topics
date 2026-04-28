package com.gtalent.helloworld.controller;

import com.gtalent.helloworld.controller.req.LoginReq;
import com.gtalent.helloworld.controller.res.LoginRes;
import com.gtalent.helloworld.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT 登入端點：POST /auth/login
 *
 * 僅在 app.auth.mode=jwt 時載入（@ConditionalOnProperty），
 * Session mode 下此 controller 不存在。
 *
 * 流程：
 *   1. 接收 { username, password }
 *   2. 透過 AuthenticationManager 驗證帳密（呼叫 UserDetailsService + BCrypt）
 *   3. 驗證成功 → 產生 JWT → 回傳 { token }
 *   4. 驗證失敗 → 回傳 401
 */
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(name = "app.auth.mode", havingValue = "jwt")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
        try {
            // 使用 Spring Security AuthenticationManager 驗證帳密
            // 內部呼叫 UserDetailsServiceImpl.loadUserByUsername() 並比對 BCrypt
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            // 驗證成功，以 username 產生 JWT
            String username = authentication.getName();
            String token = jwtUtil.generateToken(username);

            return ResponseEntity.ok(new LoginRes(token));

        } catch (AuthenticationException e) {
            // 帳號或密碼錯誤
            return ResponseEntity.status(401).body("帳號或密碼錯誤");
        }
    }
}
