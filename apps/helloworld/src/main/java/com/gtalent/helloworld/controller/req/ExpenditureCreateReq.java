package com.gtalent.helloworld.controller.req;

import java.time.LocalDate;
import java.util.List;

import com.gtalent.helloworld.domain.valueobject.PaymentMethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ExpenditureCreateReq {

    @NotBlank
    private String name;

    @Min(0)
    private int money;

    @NotNull
    private PaymentMethod payway;

    /** 支出日期，null 時預設為今日 */
    private LocalDate date;

    /** 分類名稱清單（需已存在於 categories 表，至少選擇 1 個） */
    @NotEmpty(message = "至少需選擇一個分類")
    private List<@NotBlank(message = "分類名稱不能為空") String> categoryNames;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }

    public PaymentMethod getPayway() { return payway; }
    public void setPayway(PaymentMethod payway) { this.payway = payway; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }
}
