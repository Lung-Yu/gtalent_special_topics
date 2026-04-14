package com.example.domain.valueobject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Generic pagination result wrapper
 * @param <T> The type of data in the page
 */
public class PageResult<T> {
    private final List<T> data;
    private final Cursor nextCursor;
    private final boolean hasMore;
    
    /**
     * Constructor
     * @param data The list of data in this page
     * @param nextCursor The cursor for the next page (null if no more pages)
     * @param hasMore Whether there are more pages
     */
    public PageResult(List<T> data, Cursor nextCursor, boolean hasMore) {
        this.data = data != null ? Collections.unmodifiableList(data) : Collections.emptyList();
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }
    
    /**
     * Constructor for last page (no more data)
     * @param data The list of data in this page
     */
    public PageResult(List<T> data) {
        this(data, null, false);
    }
    
    /**
     * Get the data in this page
     * @return unmodifiable list of data
     */
    public List<T> getData() {
        return data;
    }
    
    /**
     * Get the cursor for the next page
     * @return cursor or null if no more pages
     */
    public Cursor getNextCursor() {
        return nextCursor;
    }
    
    /**
     * Check if there are more pages
     * @return true if there are more pages
     */
    public boolean hasMore() {
        return hasMore;
    }
    
    /**
     * Get the encoded cursor string for the next page
     * @return encoded cursor string or null if no more pages
     */
    public String getNextCursorString() {
        return nextCursor != null ? nextCursor.encode() : null;
    }
    
    /**
     * Get the number of items in this page
     * @return size of data list
     */
    public int getSize() {
        return data.size();
    }
    
    /**
     * Check if this page is empty
     * @return true if data is empty
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageResult<?> that = (PageResult<?>) o;
        return hasMore == that.hasMore &&
                Objects.equals(data, that.data) &&
                Objects.equals(nextCursor, that.nextCursor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(data, nextCursor, hasMore);
    }
    
    @Override
    public String toString() {
        return "PageResult{" +
                "size=" + data.size() +
                ", hasMore=" + hasMore +
                ", nextCursor=" + nextCursor +
                '}';
    }
}
