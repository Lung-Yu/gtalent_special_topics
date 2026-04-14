package com.gtalent.helloworld.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gtalent.helloworld.service.Order;

public interface OrderRepositoryImpl extends CrudRepository<Order, Integer> {

    @Query(value = "SELECT id, name FROM orders", nativeQuery = true)
    List<OrderSummary> findAllIdAndName();

}
