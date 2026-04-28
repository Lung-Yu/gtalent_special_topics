package com.gtalent.helloworld.controller.res;

/**
 * JWT 登入回應 body：{ "token": "eyJ..." }
 */
public class LoginRes {

    private final String token;

    public LoginRes(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}
