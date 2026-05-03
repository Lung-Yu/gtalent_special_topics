
package com.gtalent.helloworld.service;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtalent.helloworld.repository.VerifyCodeRepository;
import com.gtalent.helloworld.service.entities.VerifyCode;

/**
 * TOTP-like（Time-based One-Time Password）驗證碼服務。
 *
 * <p>核心設計：
 * <ul>
 *   <li>每次 generate 產生一組隨機 HMAC secret，存入 DB</li>
 *   <li>以 HMAC-SHA256(secret, timeStep) 計算 6 位數 code，timeStep = Unix 秒 / 60</li>
 *   <li>validate 時從 DB 取最新 secret，走相同計算路徑比對</li>
 *   <li>允許「當前分鐘」或「前一分鐘」，避免跨分鐘邊界失效</li>
 *   <li>serviceId 欄位保留，未來多服務可各自持有不同 secret</li>
 * </ul>
 */
@Service
public class VerifyCodeService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final int CODE_DIGITS = 6;
    private static final int SECRET_BYTES = 20;   // 160 bits，符合 RFC 4226 建議
    private static final String DEFAULT_SERVICE = "default";

    @Autowired
    private VerifyCodeRepository verifyCodeRepository;

    // -------------------------------------------------------------------------
    // TOTP 核心計算
    // -------------------------------------------------------------------------

    /**
     * 以指定 secret（Base64）與 time-step 計算 TOTP code。
     * 演算法與 RFC 6238 dynamic truncation 相同。
     */
    private String computeCode(String secretBase64, long timeStep) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGO);
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(keySpec);

            byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int truncated = ((hash[offset]     & 0x7F) << 24)
                          | ((hash[offset + 1] & 0xFF) << 16)
                          | ((hash[offset + 2] & 0xFF) << 8)
                          |  (hash[offset + 3] & 0xFF);

            int divisor = (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", truncated % divisor);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute verify code", e);
        }
    }

    /** 取得當前分鐘的 time-step（Unix 秒除以 60） */
    private long currentTimeStep() {
        return Instant.now().getEpochSecond() / 60;
    }

    /** 產生 cryptographically-secure 隨機 secret，以 Base64 編碼回傳 */
    private String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // -------------------------------------------------------------------------
    // 公開 API
    // -------------------------------------------------------------------------

    /**
     * 為指定服務產生驗證碼。
     * 每次呼叫都會產生新的隨機 secret 並存入 DB。
     *
     * @param serviceId 服務識別碼，預設傳入 "default"
     * @return 6 位數 TOTP code
     */
    public String generateVerifyCode(String serviceId) {
        String secret = generateSecret();

        VerifyCode entity = new VerifyCode();
        entity.setSecret(secret);
        entity.setServiceId(serviceId);
        entity.setLastUpdatedAt(LocalDateTime.now());
        verifyCodeRepository.save(entity);

        return computeCode(secret, currentTimeStep());
    }

    /** 以預設服務產生驗證碼（向下相容） */
    public String generateVerifyCode() {
        return generateVerifyCode(DEFAULT_SERVICE);
    }

    /**
     * 驗證輸入的 code 是否與指定服務最新的 secret 吻合。
     * 允許「當前分鐘」或「前一分鐘」的 code。
     *
     * @param serviceId 服務識別碼
     * @param code      使用者輸入的 6 位數驗證碼
     */
    public boolean validateVerifyCode(String serviceId, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        Optional<VerifyCode> latest = verifyCodeRepository
                .findTopByServiceIdOrderByLastUpdatedAtDesc(serviceId);
        if (latest.isEmpty()) {
            return false;
        }
        String secret = latest.get().getSecret();
        long timeStep = currentTimeStep();
        return computeCode(secret, timeStep).equals(code)
            || computeCode(secret, timeStep - 1).equals(code);
    }

    /** 以預設服務驗證（向下相容） */
    public boolean validateVerifyCode(String code) {
        return validateVerifyCode(DEFAULT_SERVICE, code);
    }
}
