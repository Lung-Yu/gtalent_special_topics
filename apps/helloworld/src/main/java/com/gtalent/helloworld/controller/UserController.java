package com.gtalent.helloworld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gtalent.helloworld.service.UserService;
import com.gtalent.helloworld.service.entities.User;
import com.gtalent.helloworld.controller.req.UserCreationReq;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestController
@RequestMapping("v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;


    @PostMapping
    public User createUser(@Valid @RequestBody UserCreationReq user_req) {
        // Implementation for creating a new user
        User user = new User();
        user.setUsername(user_req.getUsername());
        user.setEmail(user_req.getEmail());
        user.setPassword(user_req.getPassword());

        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        // Implementation for deleting a user
        userService.deleteUser(id);
    }
    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        // Implementation for updating an existing user
        user.setId(id);
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        // Implementation for retrieving users
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        // Implementation for retrieving a specific user by ID
        return userService.getUserById(id);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {

        // 建立一個 Map 來存放欄位名稱和對應的錯誤訊息
        Map<String, String> errors = new HashMap<>();

        // 從例外中取得所有的欄位錯誤，並將其欄位名稱和錯誤訊息放入 Map 中
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        // 回傳包含錯誤訊息的 Map，並設定 HTTP 狀態碼為 400 Bad Request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

}
