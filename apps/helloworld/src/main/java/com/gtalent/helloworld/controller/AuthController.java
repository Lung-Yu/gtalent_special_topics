package com.gtalent.helloworld.controller;

import com.gtalent.helloworld.controller.req.AuthVerifyReq;
import com.gtalent.helloworld.controller.req.LoginReq;
import com.gtalent.helloworld.controller.res.LoginInitRes;
import com.gtalent.helloworld.controller.res.LoginRes;
import com.gtalent.helloworld.security.jwt.JwtUtil;
import com.gtalent.helloworld.service.PendingAuthService;
import com.gtalent.helloworld.service.VerifyCodeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
 * 登入端點（JWT mode）：兩步驟 MFA 流程。
 *
 * Step 1  POST /auth/login   → 帳密驗證，通過後產生 TOTP code，回傳 MFA_REQUIRED
 * Step 2  POST /auth/verify  → 驗證 6 碼，通過後發 JWT（HttpOnly Cookie + body）
 */
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(name = "app.auth.mode", havingValue = "jwt")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final VerifyCodeService verifyCodeService;
    private final PendingAuthService pendingAuthService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          VerifyCodeService verifyCodeService,
                          PendingAuthService pendingAuthService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.verifyCodeService = verifyCodeService;
        this.pendingAuthService = pendingAuthService;
    }

    /**
     * Step 1：驗證帳號密碼。
     * 成功 → 產生 TOTP code（存 DB），標記 pending，回傳 { status: "MFA_REQUIRED", verifyCode }
     * 失敗 → 401
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            String username = authentication.getName();

            // 產生 TOTP code 並存 DB
            String code = verifyCodeService.generateVerifyCode(username);

            // 標記此 username 正在等待 2FA（TTL 5 分鐘）
            pendingAuthService.markPending(username);

            return ResponseEntity.ok(new LoginInitRes("MFA_REQUIRED", code));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("帳號或密碼錯誤");
        }
    }

    /**
     * Step 2：驗證 6 碼 TOTP。
     * 成功 → 發 JWT（HttpOnly Cookie），回傳 { token }
     * 失敗 → 401
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody AuthVerifyReq req,
                                    HttpServletResponse response) {
        String username = req.getUsername();

        // 確認此 username 確實通過了 Step 1
        if (!pendingAuthService.isPending(username)) {
            return ResponseEntity.status(401).body("請先完成帳密驗證");
        }

        // 驗證 TOTP code（使用 username 作為 serviceId）
        if (!verifyCodeService.validateVerifyCode(username, req.getCode())) {
            return ResponseEntity.status(401).body("驗證碼錯誤或已過期");
        }

        // 全部通過，清除 pending 記錄
        pendingAuthService.clear(username);

        // 產生 JWT
        String token = jwtUtil.generateToken(username);

        // 設定 HttpOnly Cookie（瀏覽器自動帶，防 XSS）
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtUtil.getExpirationSeconds());
        response.addCookie(cookie);

        return ResponseEntity.ok(new LoginRes(token));
    }

    /**
     * Step 2 輔助：在 pending 期間重新產生驗證碼（前端倒數到期自動呼叫）。
     * 只接受仍在 pending 狀態的 username，防止任意人重置他人的 code。
     */
    @PostMapping("/refresh-code")
    public ResponseEntity<?> refreshCode(@RequestBody java.util.Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("缺少 username");
        }
        if (!pendingAuthService.isPending(username)) {
            return ResponseEntity.status(401).body("請先完成帳密驗證");
        }
        String code = verifyCodeService.generateVerifyCode(username);
        return ResponseEntity.ok(new LoginInitRes("MFA_REQUIRED", code));
    }
}
