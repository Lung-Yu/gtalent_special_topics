package com.gtalent.helloworld.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    List<Product> findByNameContaining(String name);

    @Override
    @EntityGraph(attributePaths = {"createdBy"})
    List<Product> findAll();
}
