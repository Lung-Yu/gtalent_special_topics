package com.gtalent.helloworld.service.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 儲存每次 generateVerifyCode() 所產生的 HMAC 密鑰。
 * secret 欄位為隨機產生的 Base64 字串，用於 TOTP 計算。
 * serviceId 欄位保留給未來多服務場景使用。
 */
@Entity
public class VerifyCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /** HMAC 密鑰，Base64 編碼，每次 generate 時隨機產生 */
    @Column(nullable = false)
    private String secret;

    /** 預留給未來多服務場景，預設為 "default" */
    @Column(nullable = false)
    private String serviceId = "default";

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
