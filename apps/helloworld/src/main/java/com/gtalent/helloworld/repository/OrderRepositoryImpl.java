package com.gtalent.helloworld.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gtalent.helloworld.service.Order;

import com.gtalent.helloworld.repository.OrderSummary;

public interface OrderRepositoryImpl extends CrudRepository<Order, Integer> {

    @Query(value = "SELECT id, name FROM orders", nativeQuery = true)
    List<OrderSummary> findAllIdAndName();

    @Query(value = "SELECT id, name FROM orders WHERE name = ?1", nativeQuery = true)
    List<OrderSummary> findByName(String name);

    // @Query(value = "SELECT id, name FROM orders WHERE name = ?1", nativeQuery = true)
    @Query(value = "SELECT id, name FROM orders WHERE name = ?1 OR (created_at >= ?2 AND created_at <= ?3)", nativeQuery = true)
    List<OrderSummary> findByNameBetweenStartDateAndEndDate(String name, LocalDateTime startDateTime, LocalDateTime endDateTime);

}
