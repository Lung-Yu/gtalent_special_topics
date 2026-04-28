package com.gtalent.helloworld.controller.req;

import jakarta.validation.constraints.NotBlank;

/**
 * JWT 登入請求 body：{ "username": "...", "password": "..." }
 */
public class LoginReq {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
