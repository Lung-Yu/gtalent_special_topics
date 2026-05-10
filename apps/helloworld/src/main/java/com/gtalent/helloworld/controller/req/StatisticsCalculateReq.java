package com.gtalent.helloworld.controller.req;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StatisticsCalculateReq {

    /** 統計策略類型：USER 或 MANAGER */
    @NotBlank
    private String type;

    /** 統計涵蓋日期，null 時預設為昨日 */
    @NotNull
    private LocalDate date;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
