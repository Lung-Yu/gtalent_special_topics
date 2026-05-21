package com.gtalent.helloworld.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtalent.helloworld.controller.req.BudgetSettingReq;
import com.gtalent.helloworld.domain.model.BudgetSetting;
import com.gtalent.helloworld.service.BudgetAlertService;
import com.gtalent.helloworld.service.UserRepository;
import com.gtalent.helloworld.service.entities.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budget-settings")
public class BudgetSettingController {

    private final BudgetAlertService budgetAlertService;
    private final UserRepository userRepository;

    public BudgetSettingController(BudgetAlertService budgetAlertService, UserRepository userRepository) {
        this.budgetAlertService = budgetAlertService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public BudgetSetting get(Authentication auth) {
        User user = resolveUser(auth);
        return budgetAlertService.getOrDefault(user.getId());
    }

    @PutMapping
    public BudgetSetting save(@Valid @RequestBody BudgetSettingReq req, Authentication auth) {
        User user = resolveUser(auth);
        return budgetAlertService.saveSetting(
                user.getId(), req.getThreshold(), req.getAlertHour(), req.getAlertMinute(), req.isEnabled());
    }

    private User resolveUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登入");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));
    }
}
