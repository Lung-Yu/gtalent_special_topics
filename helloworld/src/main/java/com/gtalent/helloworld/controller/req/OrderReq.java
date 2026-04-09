package com.gtalent.helloworld.controller.req;

import io.micrometer.common.lang.NonNull;

public class OrderReq {

    private int id;

    @NonNull
    private String name;

    @NonNull
    private int quantity;

    @NonNull
    private double price;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    
}
