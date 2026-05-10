package com.gtalent.helloworld.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtalent.helloworld.controller.req.ExpenditureCreateReq;
import com.gtalent.helloworld.controller.resp.ExpenditureResp;
import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.service.ExpenditureService;
import com.gtalent.helloworld.service.UserRepository;
import com.gtalent.helloworld.service.entities.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenditures")
public class ExpenditureController {

    private final ExpenditureService expenditureService;
    private final UserRepository userRepository;

    public ExpenditureController(ExpenditureService expenditureService, UserRepository userRepository) {
        this.expenditureService = expenditureService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/expenditures
     * 建立支出記錄，所屬使用者為當前登入者。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenditureResp create(@Valid @RequestBody ExpenditureCreateReq req, Authentication auth) {
        User user = resolveUser(auth);
        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();
        ExpenditureRecord record = expenditureService.create(
                user, req.getName(), req.getMoney(), req.getPayway(), date, req.getCategoryNames());
        return ExpenditureResp.from(record);
    }

    /**
     * GET /api/expenditures
     * 分頁查詢當前使用者的所有支出記錄。
     * 支援 ?page=0&size=10&sort=date,desc
     */
    @GetMapping
    public Page<ExpenditureResp> findAll(
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {

        User user = resolveUser(auth);
        return expenditureService.findByUser(user, pageable)
                .map(ExpenditureResp::from);
    }

    /**
     * GET /api/expenditures/by-date?date=2026-05-10
     * 查詢當前使用者指定日期的支出記錄。
     */
    @GetMapping("/by-date")
    public List<ExpenditureResp> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {

        User user = resolveUser(auth);
        return expenditureService.findByUserAndDate(user, date).stream()
                .map(ExpenditureResp::from)
                .collect(Collectors.toList());
    }

    /**
     * DELETE /api/expenditures/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenditureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登入");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));
    }
}
