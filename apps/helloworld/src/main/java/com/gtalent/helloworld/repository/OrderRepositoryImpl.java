package com.gtalent.helloworld.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gtalent.helloworld.service.Order;

public interface OrderRepositoryImpl extends JpaRepository<Order, Integer> {

    @Query(value = "SELECT id, name FROM orders",
           countQuery = "SELECT COUNT(*) FROM orders",
           nativeQuery = true)
    Page<OrderSummary> findAllIdAndName(Pageable pageable);

    @Query(value = "SELECT id, name FROM orders WHERE name = ?1 OR (created_at >= ?2 AND created_at <= ?3)",
           countQuery = "SELECT COUNT(*) FROM orders WHERE name = ?1 OR (created_at >= ?2 AND created_at <= ?3)",
           nativeQuery = true)
    Page<OrderSummary> findByNameBetweenStartDateAndEndDate(String name, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

}
