package com.gtalent.helloworld.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TodoController {
    
    @GetMapping("/todos")
    public ResponseEntity<String> getTodos() {
        return ResponseEntity.ok("Get all todos");
    }

    @PostMapping("/todos")
    public ResponseEntity<String> createTodo(@RequestBody String todo) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Todo created: " + todo);
    }

    @PutMapping("/todos/{id}")
    public ResponseEntity<String> updateTodo(@PathVariable String id, @RequestBody String todo) {
        return ResponseEntity.ok("Todo updated: " + id);
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable String id) {
        return ResponseEntity.noContent().build();
    }

}
