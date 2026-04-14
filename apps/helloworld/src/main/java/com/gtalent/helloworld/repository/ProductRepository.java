package com.gtalent.helloworld.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.entities.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {

    List<Product> findByNameContaining(String name);
    
}
