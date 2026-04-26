package com.gtalent.helloworld.service;

import com.gtalent.helloworld.service.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 實作 Spring Security 的 UserDetailsService 介面，
 * 由 Spring Security 在驗證時自動呼叫 loadUserByUsername()，
 * 從資料庫查詢使用者並建立 UserDetails 物件供框架進行密碼比對。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 從資料庫依 username 查詢使用者
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "找不到使用者：" + username));

        // 將 User entity 包裝成 Spring Security 的 UserDetails 物件
        // 框架會自動比對 UserDetails 內的 password 與輸入密碼（BCrypt）
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
