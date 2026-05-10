package com.gtalent.helloworld.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gtalent.helloworld.controller.req.StatisticsCalculateReq;
import com.gtalent.helloworld.domain.model.StatisticsPoint;
import com.gtalent.helloworld.service.StatisticsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * POST /api/statistics/calculate
     * 手動觸發統計計算並儲存結果。
     * Body: { "type": "USER", "date": "2026-05-09" }
     */
    @PostMapping("/calculate")
    @ResponseStatus(HttpStatus.CREATED)
    public List<StatisticsPoint> calculate(@Valid @RequestBody StatisticsCalculateReq req) {
        return statisticsService.calculateAndSave(req.getType(), req.getDate());
    }

    /**
     * GET /api/statistics
     * 查詢所有統計點。
     */
    @GetMapping
    public List<StatisticsPoint> findAll() {
        return statisticsService.findAll();
    }

    /**
     * GET /api/statistics?date=2026-05-09
     * 查詢指定日期的統計點。
     */
    @GetMapping(params = "date")
    public List<StatisticsPoint> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return statisticsService.findByDate(date);
    }
}
