package com.example.application.exception;

public class TagTypeNotExists extends Exception {
    public TagTypeNotExists(String type) {
        super("Tag type not exists: " + type);
    }
}
