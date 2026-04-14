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

@RestController
public class UserController {
    
    @Autowired
    private UserService userService;


    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        // Implementation for creating a new user

        return userService.createUser(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        // Implementation for deleting a user
        userService.deleteUser(id);
    }
    
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        // Implementation for updating an existing user
        user.setId(id);
        return userService.updateUser(user);
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        // Implementation for retrieving users
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        // Implementation for retrieving a specific user by ID
        return userService.getUserById(id);
    }


}
