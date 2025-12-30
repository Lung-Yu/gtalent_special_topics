package com.example.domain.repository;

import java.util.List;

import com.example.domain.model.User;

public interface UserRepository {
    List<User> findAll();
    void save(User user);
}
