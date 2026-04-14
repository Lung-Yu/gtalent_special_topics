package com.gtalent.helloworld.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gtalent.helloworld.controller.req.OrderReq;
import com.gtalent.helloworld.controller.resp.OrderResp;
import com.gtalent.helloworld.service.OrderService;

@RestController
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResp> createOrder(@Validated @RequestBody OrderReq orderDto) {
        // Implementation for creating an order
        OrderResp order = orderService.createOrder(orderDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.getId())
                .toUri();

        return ResponseEntity.created(location).body(order);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int id) {
        // Implementation for deleting an order by ID
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResp> updateOrder(@PathVariable int id, @Validated @RequestBody OrderReq orderDto) {
        // Implementation for updating an order by ID
        OrderResp updatedOrder = orderService.updateOrder(id, orderDto);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/orders")
    public List<OrderResp> getOrders(@RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "start", required = false) LocalDate start,
                                     @RequestParam(value = "end", required = false) LocalDate end) {
        // Implementation for retrieving orders

       LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
       LocalDateTime endDateTime = end != null ? end.atTime(23,59, 59) : null;

        return orderService.getOrders(name,startDateTime, endDateTime);
    
    }

    @GetMapping("/orders/{id}")
    public OrderResp getOrderById(@PathVariable int id) {
        // Implementation for retrieving a specific order by ID
        return orderService.getOrderById(id);
    }

}
