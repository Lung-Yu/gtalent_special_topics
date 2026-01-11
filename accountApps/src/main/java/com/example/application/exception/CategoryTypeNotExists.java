package com.example.application.exception;

public class CategoryTypeNotExists extends Exception {
    public CategoryTypeNotExists(String type) {
        super("Category type not exists: " + type);
    }
}
