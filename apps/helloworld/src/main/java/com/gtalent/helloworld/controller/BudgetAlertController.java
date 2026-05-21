package com.gtalent.helloworld.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtalent.helloworld.domain.model.BudgetAlertLog;
import com.gtalent.helloworld.domain.valueobject.AlertStatus;
import com.gtalent.helloworld.repository.BudgetAlertLogRepository;
import com.gtalent.helloworld.service.UserRepository;
import com.gtalent.helloworld.service.entities.User;

@RestController
@RequestMapping("/api/budget-alerts")
public class BudgetAlertController {

    private final BudgetAlertLogRepository budgetAlertLogRepository;
    private final UserRepository userRepository;

    public BudgetAlertController(BudgetAlertLogRepository budgetAlertLogRepository,
                                 UserRepository userRepository) {
        this.budgetAlertLogRepository = budgetAlertLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/budget-alerts/today
     * 回傳今日 SENT 狀態的 alert log；不存在或尚未觸發則 204。
     */
    @GetMapping("/today")
    public ResponseEntity<BudgetAlertLog> getToday(Authentication auth) {
        User user = resolveUser(auth);
        Optional<BudgetAlertLog> alert = budgetAlertLogRepository
                .findByUserIdAndDate(user.getId(), LocalDate.now());
        return alert
                .filter(a -> a.getStatus() == AlertStatus.SENT)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    private User resolveUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登入");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));
    }
}
