package com.example.application.exception;

public class DuplicateTagException extends RuntimeException {
    public DuplicateTagException(String message) {
        super(message);
    }

    public DuplicateTagException(String name, String type) {
        super(String.format("Tag with name '%s' and type '%s' already exists", name, type));
    }
}
