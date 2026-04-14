package com.gtalent.helloworld.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @EntityGraph(value = "User.products")
    List<User> findAll();

    @Override
    @EntityGraph(value = "User.products")
    Optional<User> findById(Long id);

}
