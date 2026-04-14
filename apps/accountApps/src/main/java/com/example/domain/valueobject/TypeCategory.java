package com.example.domain.valueobject;

public enum TypeCategory {
    INCOME,
    OUTCOME;

    public static TypeCategory fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Category type cannot be null");
        }
        switch (value.trim().toUpperCase()) {
            case "INCOME":
                return INCOME;
            case "OUTCOME":
                return OUTCOME;
            default:
                throw new IllegalArgumentException("Unknown category type: " + value);
        }
    }
}
