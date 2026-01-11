package com.example.application.exception;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String message) {
        super(message);
    }

    public DuplicateCategoryException(String name, String type) {
        super(String.format("Category with name '%s' and type '%s' already exists", name, type));
    }
}
