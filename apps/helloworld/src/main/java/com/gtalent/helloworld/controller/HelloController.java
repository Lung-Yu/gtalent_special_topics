package com.gtalent.helloworld.controller;

import com.gtalent.helloworld.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Random;

@Controller
public class HelloController {

    private static final List<String> NAMES = List.of(
            "Alice", "Bob", "Charlie", "Diana", "Edward",
            "Fiona", "George", "Hannah", "Ivan", "Julia"
    );

    private static final Random RANDOM = new Random();

    @Autowired
    private ProductService productService;

    /**
     * 此路徑受 Spring Security 保護：未登入會自動轉址到 /login。
     * 登入後 Spring Security 的 Session 中已有身份資料，
     * 使用者名稱由 hello.html 透過 sec:authentication="name" 直接從 SecurityContext 取得。
     */
    @GetMapping("/hello")
    public String hello(Model model) {
        String userName = NAMES.get(RANDOM.nextInt(NAMES.size()));
        model.addAttribute("userName", userName);
        model.addAttribute("allProducts", productService.getProducts());
        return "hello";
    }
}
