package com.gtalent.helloworld.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    Page<Product> findByNameContaining(String name, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Product> findAll(Pageable pageable);
}
