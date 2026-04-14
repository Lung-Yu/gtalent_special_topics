package com.example.domain.valueobject;

import com.example.application.exception.InvalidCursorException;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for Cursor class
 */
public class CursorTest {
    
    @Test
    public void testEncodeDecode() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 8);
        Long id = 12345L;
        Cursor cursor = new Cursor(date, id);
        
        // Act
        String encoded = cursor.encode();
        Cursor decoded = Cursor.decode(encoded);
        
        // Assert
        assertEquals(date, decoded.getDate());
        assertEquals(id, decoded.getId());
    }
    
    @Test
    public void testEncodeProducesBase64() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 8);
        Long id = 12345L;
        Cursor cursor = new Cursor(date, id);
        
        // Act
        String encoded = cursor.encode();
        
        // Assert - Base64 should not contain special characters except =
        assertTrue(encoded.matches("^[A-Za-z0-9+/=]+$"));
    }
    
    @Test(expected = InvalidCursorException.class)
    public void testDecodeNullThrowsException() {
        Cursor.decode(null);
    }
    
    @Test(expected = InvalidCursorException.class)
    public void testDecodeEmptyStringThrowsException() {
        Cursor.decode("");
    }
    
    @Test(expected = InvalidCursorException.class)
    public void testDecodeInvalidBase64ThrowsException() {
        Cursor.decode("not-valid-base64!");
    }
    
    @Test(expected = InvalidCursorException.class)
    public void testDecodeInvalidFormatThrowsException() {
        // Valid Base64 but wrong format (missing colon separator)
        String invalidCursor = java.util.Base64.getEncoder().encodeToString("2026-03-08_12345".getBytes());
        Cursor.decode(invalidCursor);
    }
    
    @Test(expected = InvalidCursorException.class)
    public void testDecodeInvalidDateThrowsException() {
        // Valid Base64 with invalid date
        String invalidCursor = java.util.Base64.getEncoder().encodeToString("invalid-date:12345".getBytes());
        Cursor.decode(invalidCursor);
    }
    
    @Test(expected = InvalidCursorException.class)
    public void testDecodeInvalidIdThrowsException() {
        // Valid Base64 with invalid ID
        String invalidCursor = java.util.Base64.getEncoder().encodeToString("2026-03-08:not-a-number".getBytes());
        Cursor.decode(invalidCursor);
    }
    
    @Test
    public void testEquals() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 8);
        Long id = 12345L;
        Cursor cursor1 = new Cursor(date, id);
        Cursor cursor2 = new Cursor(date, id);
        Cursor cursor3 = new Cursor(date, 99999L);
        
        // Assert
        assertEquals(cursor1, cursor2);
        assertNotEquals(cursor1, cursor3);
    }
    
    @Test
    public void testHashCode() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 8);
        Long id = 12345L;
        Cursor cursor1 = new Cursor(date, id);
        Cursor cursor2 = new Cursor(date, id);
        
        // Assert
        assertEquals(cursor1.hashCode(), cursor2.hashCode());
    }
    
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullDate() {
        new Cursor(null, 12345L);
    }
    
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullId() {
        new Cursor(LocalDate.now(), null);
    }
}
