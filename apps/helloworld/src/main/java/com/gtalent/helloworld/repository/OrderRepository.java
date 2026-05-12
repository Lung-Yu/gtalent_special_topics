package com.gtalent.helloworld.repository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.Order;

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

    public Page<OrderSummary> findByStartDateTimeAndEndDateTime(
            String name,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable) {

        return orderRepositoryImpl.findByNameBetweenStartDateAndEndDate(name, startDateTime, endDateTime, pageable);
    }

    public Page<OrderSummary> findAll(Pageable pageable) {
        return orderRepositoryImpl.findAllIdAndName(pageable);
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
