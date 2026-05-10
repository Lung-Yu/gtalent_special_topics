package com.gtalent.helloworld.controller.resp;

import java.time.LocalDate;
import java.util.List;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.domain.valueobject.PaymentMethod;

public class ExpenditureResp {

    private Long id;
    private String username;
    private String name;
    private int money;
    private PaymentMethod payway;
    private LocalDate date;
    private List<String> categoryNames;

    public static ExpenditureResp from(ExpenditureRecord expenditureRecord) {
        ExpenditureResp resp = new ExpenditureResp();
        resp.id = expenditureRecord.getId();
        resp.username = expenditureRecord.getUser() != null ? expenditureRecord.getUser().getUsername() : null;
        resp.name = expenditureRecord.getName();
        resp.money = expenditureRecord.getMoney();
        resp.payway = expenditureRecord.getPayway();
        resp.date = expenditureRecord.getDate();
        resp.categoryNames = expenditureRecord.getCategories().stream()
                .map(Category::getName)
                .toList();
        return resp;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public int getMoney() { return money; }
    public PaymentMethod getPayway() { return payway; }
    public LocalDate getDate() { return date; }
    public List<String> getCategoryNames() { return categoryNames; }
}
