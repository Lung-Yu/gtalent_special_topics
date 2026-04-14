package com.example.domain.valueobject;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.valueobject.UserIdentity;
import com.example.domain.valueobject.PaymentMethod;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for PageResult class
 */
public class PageResultTest {
    
    @Test
    public void testConstructorWithData() {
        // Arrange
        ExpenditureRecord record = createSampleRecord();
        List<ExpenditureRecord> data = Arrays.asList(record);
        Cursor cursor = new Cursor(LocalDate.now(), 123L);
        
        // Act
        PageResult<ExpenditureRecord> result = new PageResult<>(data, cursor, true);
        
        // Assert
        assertEquals(1, result.getSize());
        assertEquals(cursor, result.getNextCursor());
        assertTrue(result.hasMore());
        assertFalse(result.isEmpty());
    }
    
    @Test
    public void testConstructorForLastPage() {
        // Arrange
        ExpenditureRecord record = createSampleRecord();
        List<ExpenditureRecord> data = Arrays.asList(record);
        
        // Act
        PageResult<ExpenditureRecord> result = new PageResult<>(data);
        
        // Assert
        assertEquals(1, result.getSize());
        assertNull(result.getNextCursor());
        assertFalse(result.hasMore());
    }
    
    @Test
    public void testGetNextCursorString() {
        // Arrange
        Cursor cursor = new Cursor(LocalDate.of(2026, 3, 8), 12345L);
        PageResult<ExpenditureRecord> result = new PageResult<>(
            Arrays.asList(createSampleRecord()), cursor, true);
        
        // Act
        String cursorString = result.getNextCursorString();
        
        // Assert
        assertNotNull(cursorString);
        assertEquals(cursor.encode(), cursorString);
    }
    
    @Test
    public void testGetNextCursorStringReturnsNullWhenNoCursor() {
        // Arrange
        PageResult<ExpenditureRecord> result = new PageResult<>(
            Arrays.asList(createSampleRecord()));
        
        // Act
        String cursorString = result.getNextCursorString();
        
        // Assert
        assertNull(cursorString);
    }
    
    @Test
    public void testEmptyPage() {
        // Act
        PageResult<ExpenditureRecord> result = new PageResult<>(Collections.emptyList());
        
        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.getSize());
        assertFalse(result.hasMore());
    }
    
    @Test
    public void testDataIsUnmodifiable() {
        // Arrange
        ExpenditureRecord record = createSampleRecord();
        List<ExpenditureRecord> data = Arrays.asList(record);
        PageResult<ExpenditureRecord> result = new PageResult<>(data);
        
        // Act & Assert
        try {
            result.getData().add(createSampleRecord());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test
    public void testNullDataBecomesEmptyList() {
        // Act
        PageResult<ExpenditureRecord> result = new PageResult<>(null, null, false);
        
        // Assert
        assertNotNull(result.getData());
        assertTrue(result.isEmpty());
    }
    
    private ExpenditureRecord createSampleRecord() {
        return new ExpenditureRecord(
            UserIdentity.of("testuser"),
            "Test Expenditure",
            100,
            Arrays.asList("飲食"),
            PaymentMethod.LinePay,
            LocalDate.now()
        );
    }
}
