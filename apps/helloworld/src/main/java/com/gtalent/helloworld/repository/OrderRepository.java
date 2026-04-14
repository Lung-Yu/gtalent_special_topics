package com.gtalent.helloworld.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.Order;

import com.gtalent.helloworld.repository.OrderSummary;

@Repository
public class OrderRepository {

    @Autowired
    private OrderRepositoryImpl orderRepositoryImpl;

    // private int count = 0;

    // private List<Order> orders = new ArrayList<>();

    public Order save(Order order) {

        // order.setId(++count);
        // orders.add(order);

        // return order;

        return orderRepositoryImpl.save(order);
    }

    public List<OrderSummary> findByStartDateTimeAndEndDateTime(
            String name,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        return orderRepositoryImpl.findByNameBetweenStartDateAndEndDate(name, startDateTime, endDateTime);
    }

    public List<OrderSummary> findAll() {

        // return this.orders;
        return orderRepositoryImpl.findAllIdAndName();
    }

    public Order findById(int id) {

        // for (Order order : orders) {
        // if (order.getId() == id) {
        // return order;
        // }
        // }

        // return null;

        return orderRepositoryImpl.findById(id).orElse(null);
    }

    public void deleteById(int id) {
        // orders.removeIf(order -> order.getId() == id);
        orderRepositoryImpl.deleteById(id);
    }

}
