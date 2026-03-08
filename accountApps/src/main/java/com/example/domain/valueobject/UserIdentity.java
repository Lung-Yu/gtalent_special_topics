package com.example.domain.valueobject;

import com.example.domain.model.User;
import java.util.Objects;

/**
 * 使用者身份識別值物件
 * 用於統計場景中只需要使用者識別資訊而不需要完整使用者實體的情況
 * 
 * 採用不可變設計，確保執行緒安全性和值物件語意
 */
public final class UserIdentity {
    private final String username;
    
    private UserIdentity(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.username = username;
    }
    
    /**
     * 從使用者名稱建立使用者身份
     * 
     * @param username 使用者名稱
     * @return UserIdentity 實例
     */
    public static UserIdentity of(String username) {
        return new UserIdentity(username);
    }
    
    /**
     * 從 User 實體建立使用者身份
     * 
     * @param user 使用者實體
     * @return UserIdentity 實例
     */
    public static UserIdentity from(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return new UserIdentity(user.getUsername());
    }
    
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserIdentity that = (UserIdentity) o;
        return Objects.equals(username, that.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
    
    @Override
    public String toString() {
        return "UserIdentity{username='" + username + "'}";
    }
}
