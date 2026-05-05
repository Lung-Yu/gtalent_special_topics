package com.gtalent.helloworld.controller.res;

/**
 * 帳密驗證成功，等待 2FA 的回應。
 * status = "MFA_REQUIRED"
 * verifyCode 為本次產生的 6 碼（開發/展示用，正式環境透過其他管道送出）
 */
public class LoginInitRes {
    private final String status;
    private final String verifyCode;

    public LoginInitRes(String status, String verifyCode) {
        this.status = status;
        this.verifyCode = verifyCode;
    }

    public String getStatus() { return status; }
    public String getVerifyCode() { return verifyCode; }
}
