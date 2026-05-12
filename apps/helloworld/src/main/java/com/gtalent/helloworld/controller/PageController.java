package com.gtalent.helloworld.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理 UI 頁面路由。所有路徑皆受 JWT 保護（/pages/** 需要認證）。
 * 實際資料由各頁面的 JavaScript 透過 Fetch API 向 /api/** 取得。
 */
@Controller
@RequestMapping("/pages")
public class PageController {

    @GetMapping("/categories")
    public String categories() {
        return "categories";
    }

    @GetMapping("/expenditures")
    public String expenditures() {
        return "expenditures";
    }

    @GetMapping("/statistics")
    public String statistics() {
        return "statistics";
    }

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory";
    }
}
