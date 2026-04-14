package com.example.infrastructure.persistence;

import com.example.application.exception.DatabaseConnectionException;
import com.example.domain.model.User;
import com.example.domain.repository.UserRepository;
import com.example.infrastructure.util.CaesarCipher;
import com.example.infrastructure.util.DatabaseConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL 資料庫實作的使用者儲存庫
 * 負責將使用者資料儲存至 MySQL 資料庫並從中讀取
 * 密碼使用 Caesar Cipher 加密儲存
 */
public class MySQLUserRepository implements UserRepository {

    /**
     * 查詢所有使用者
     * 
     * @return 所有使用者列表
     * @throws DatabaseConnectionException 當資料庫操作失敗時
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, password FROM users";
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                String encryptedPassword = rs.getString("password");
                
                // 解密密碼
                String decryptedPassword = CaesarCipher.decrypt(encryptedPassword);
                
                User user = new User(username, decryptedPassword);
                users.add(user);
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢使用者資料失敗: " + e.getMessage(), e);
        }
        
        return users;
    }

    /**
     * 儲存使用者
     * 如果使用者名稱已存在，則更新密碼
     * 
     * @param user 使用者物件
     * @throws DatabaseConnectionException 當資料庫操作失敗時
     */
    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE password = VALUES(password)";
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 加密密碼
            String encryptedPassword = CaesarCipher.encrypt(user.getPassword());
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, encryptedPassword);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✓ 使用者儲存成功: " + user.getUsername());
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "儲存使用者資料失敗: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根據使用者名稱查詢使用者
     * 
     * @param username 使用者名稱
     * @return Optional 包裝的使用者物件
     * @throws DatabaseConnectionException 當資料庫操作失敗時
     */
    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String sql = "SELECT username, password FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbUsername = rs.getString("username");
                    String encryptedPassword = rs.getString("password");
                    
                    // 解密密碼
                    String decryptedPassword = CaesarCipher.decrypt(encryptedPassword);
                    
                    return Optional.of(new User(dbUsername, decryptedPassword));
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                "查詢使用者失敗: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
}
