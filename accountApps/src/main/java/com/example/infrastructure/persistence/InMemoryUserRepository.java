package com.example.infrastructure.persistence;

import com.example.domain.repository.UserRepository;
import com.example.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class InMemoryUserRepository implements UserRepository {
    private List<User> users = new ArrayList<>();
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
    
    @Override
    public void save(User user) {
        users.add(user);
    }
}
