package com.example.domain.valueobject;

public enum TypeTag {
    INCOME,
    OUTCOME;

    public static TypeTag fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Tag type cannot be null");
        }
        switch (value.trim().toUpperCase()) {
            case "INCOME":
                return INCOME;
            case "OUTCOME":
                return OUTCOME;
            default:
                throw new IllegalArgumentException("Unknown tag type: " + value);
        }
    }
}
