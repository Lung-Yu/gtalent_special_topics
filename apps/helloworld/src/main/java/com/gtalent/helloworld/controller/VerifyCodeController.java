package com.gtalent.helloworld.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.gtalent.helloworld.service.VerifyCodeService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/verify-code")
public class VerifyCodeController {
    
    @Autowired
    private VerifyCodeService verifyCodeService;

    @PostMapping("/generate")
    public String generateVerifyCode() {
        // 這裡可以使用第三方庫（如 Google Authenticator）生成驗
        // 證碼，並將其與使用者綁定（例如存入資料庫）。
        // 為了簡化示例，這裡直接回傳一個固定
        // 的驗證碼，實際應用中請勿這麼做。
        return verifyCodeService.generateVerifyCode();
    }

    @PostMapping("/validate")
    public boolean validateVerifyCode(@RequestBody String code) {
        // 這裡應該從資料庫中取出與 username 綁
        // 定的驗證碼，並與傳入的 code 比對。
        // 為了簡化示例，這裡直接比對固定的驗
        // 證碼，實際應用中請勿這麼做。
        return verifyCodeService.validateVerifyCode(code);
    }
}
