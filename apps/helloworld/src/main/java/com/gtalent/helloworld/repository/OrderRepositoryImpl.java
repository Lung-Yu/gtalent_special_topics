package com.gtalent.helloworld.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.Order;

@Repository
public interface OrderRepositoryImpl extends CrudRepository<Order, Integer> {

}
