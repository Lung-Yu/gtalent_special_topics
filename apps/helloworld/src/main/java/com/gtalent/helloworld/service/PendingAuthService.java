package com.gtalent.helloworld.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 追蹤通過帳密驗證、等待輸入 2FA 驗證碼的使用者。
 * in-memory + TTL 5 分鐘，避免未完成的 2FA 永久佔用。
 */
@Service
public class PendingAuthService {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, Instant> pending = new ConcurrentHashMap<>();

    public void markPending(String username) {
        pending.put(username, Instant.now().plus(TTL));
    }

    public boolean isPending(String username) {
        Instant expiry = pending.get(username);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            pending.remove(username);
            return false;
        }
        return true;
    }

    public void clear(String username) {
        pending.remove(username);
    }
}
