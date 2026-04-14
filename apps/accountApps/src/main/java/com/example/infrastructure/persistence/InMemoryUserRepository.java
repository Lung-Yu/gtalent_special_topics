package com.example.infrastructure.persistence;

import com.example.domain.repository.UserRepository;
import com.example.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    
    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
}
