package com.gtalent.helloworld.controller;

import com.gtalent.helloworld.service.ProductService;
import com.gtalent.helloworld.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hello")
    public String hello(Model model) {
        String userName = NAMES.get(RANDOM.nextInt(NAMES.size()));
        model.addAttribute("userName", userName);
        model.addAttribute("allProducts", productService.getProducts());
        return "hello";
    }

    @PostMapping("/hello")
    public String login(@RequestParam String account,
                        @RequestParam String password,
                        Model model) {
        String userName = NAMES.get(RANDOM.nextInt(NAMES.size()));
        model.addAttribute("userName", userName);
        model.addAttribute("allProducts", productService.getProducts());
        boolean found = userRepository.findByUsernameAndPassword(account, password).isPresent();
        if (found) {
            model.addAttribute("loginMessage", "登入成功！歡迎，" + account);
            model.addAttribute("loginSuccess", true);
        } else {
            model.addAttribute("loginMessage", "帳號或密碼錯誤，請重試。");
            model.addAttribute("loginSuccess", false);
        }
        return "hello";
    }

}