package com.example.domain.valueobject;

import com.example.application.exception.InvalidCursorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Objects;

/**
 * Cursor for pagination
 * Encapsulates date + id for cursor-based pagination
 * Format: Base64(date:id) e.g., "MjAyNi0wMy0wODoxMjM0NQ==" for date=2026-03-08, id=12345
 */
public class Cursor {
    private static final String SEPARATOR = ":";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final LocalDate date;
    private final Long id;
    
    /**
     * Constructor
     * @param date The date part of the cursor
     * @param id The id part of the cursor
     */
    public Cursor(LocalDate date, Long id) {
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.id = Objects.requireNonNull(id, "ID cannot be null");
    }
    
    /**
     * Get the date
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }
    
    /**
     * Get the id
     * @return id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Encode cursor to Base64 string
     * @return Base64 encoded cursor string
     */
    public String encode() {
        String raw = date.format(DATE_FORMATTER) + SEPARATOR + id;
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }
    
    /**
     * Decode cursor from Base64 string
     * @param encodedCursor Base64 encoded cursor string
     * @return Cursor object
     * @throws InvalidCursorException if cursor format is invalid
     */
    public static Cursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.trim().isEmpty()) {
            throw new InvalidCursorException("Cursor string cannot be null or empty");
        }
        
        try {
            // Decode from Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCursor);
            String decodedString = new String(decodedBytes);
            
            // Split by separator
            String[] parts = decodedString.split(SEPARATOR);
            if (parts.length != 2) {
                throw new InvalidCursorException("Invalid cursor format: expected 'date:id', got: " + decodedString);
            }
            
            // Parse date
            LocalDate date;
            try {
                date = LocalDate.parse(parts[0], DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new InvalidCursorException("Invalid date format in cursor: " + parts[0], e);
            }
            
            // Parse id
            Long id;
            try {
                id = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                throw new InvalidCursorException("Invalid id format in cursor: " + parts[1], e);
            }
            
            return new Cursor(date, id);
            
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException("Failed to decode Base64 cursor: " + encodedCursor, e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cursor cursor = (Cursor) o;
        return Objects.equals(date, cursor.date) && Objects.equals(id, cursor.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, id);
    }
    
    @Override
    public String toString() {
        return "Cursor{" +
                "date=" + date +
                ", id=" + id +
                '}';
    }
}
