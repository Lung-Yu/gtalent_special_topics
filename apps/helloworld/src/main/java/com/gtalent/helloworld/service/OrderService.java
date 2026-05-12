package com.gtalent.helloworld.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gtalent.helloworld.controller.req.OrderReq;
import com.gtalent.helloworld.controller.resp.OrderResp;
import com.gtalent.helloworld.repository.OrderRepository;
import com.gtalent.helloworld.repository.OrderSummary;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderResp createOrder(OrderReq orderDto) {
        // Implementation for creating an order
        Order order = new Order();
        order.setName(orderDto.getName());
        order.setQuantity(orderDto.getQuantity());
        order.setPrice(orderDto.getPrice());

        Order savedOrder = orderRepository.save(order);

        OrderResp orderResp = new OrderResp();
        orderResp.setId(savedOrder.getId());
        orderResp.setName(savedOrder.getName());
        orderResp.setQuantity(savedOrder.getQuantity());
        orderResp.setPrice(savedOrder.getPrice());
        // orderResp.setCreatedAt(savedOrder.getCreatedAt());
        // orderResp.setUpdatedAt(savedOrder.getUpdatedAt());

        return orderResp;
    }

    public Page<OrderResp> getOrders(String name, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        Page<OrderSummary> orders;
        if (name == null && startDateTime == null && endDateTime == null) {
            orders = orderRepository.findAll(pageable);
        } else {
            orders = orderRepository.findByStartDateTimeAndEndDateTime(name, startDateTime, endDateTime, pageable);
        }
        return orders.map(order -> {
            OrderResp orderResp = new OrderResp();
            orderResp.setId(order.getId());
            orderResp.setName(order.getName());
            return orderResp;
        });
    }

    public OrderResp getOrderById(int id) {
        Order order = orderRepository.findById(id);
        if (order == null) {
            return null;
        }
        OrderResp orderResp = new OrderResp();
        orderResp.setId(order.getId());
        orderResp.setName(order.getName());
        orderResp.setQuantity(order.getQuantity());
        orderResp.setPrice(order.getPrice());
        // orderResp.setCreatedAt(order.getCreatedAt());
        // orderResp.setUpdatedAt(order.getUpdatedAt());
        return orderResp;
    }

    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }

    public OrderResp updateOrder(int id, OrderReq orderDto) {
        Order order = orderRepository.findById(id);
        if (order == null) {
            return null;
        }
        order.setName(orderDto.getName());
        order.setQuantity(orderDto.getQuantity());
        order.setPrice(orderDto.getPrice());
        Order updatedOrder = orderRepository.save(order);

        OrderResp orderResp = new OrderResp();
        orderResp.setId(updatedOrder.getId());
        orderResp.setName(updatedOrder.getName());
        orderResp.setQuantity(updatedOrder.getQuantity());
        orderResp.setPrice(updatedOrder.getPrice());
        // orderResp.setCreatedAt(updatedOrder.getCreatedAt());
        // orderResp.setUpdatedAt(updatedOrder.getUpdatedAt());

        return orderResp;
    }

}
