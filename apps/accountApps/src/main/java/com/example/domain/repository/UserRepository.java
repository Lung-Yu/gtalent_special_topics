package com.example.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.domain.model.User;

public interface UserRepository {
    List<User> findAll();
    void save(User user);
    
    /**
     * 根據使用者名稱查詢使用者
     * 
     * @param username 使用者名稱
     * @return Optional 包裝的使用者物件
     */
    Optional<User> findByUsername(String username);
}
