package com.example.application.command;

/**
 * Command for querying recent expenditures with pagination
 */
public class QueryRecentExpendituresCommand {
    private final String username;
    private final String cursorString;
    private final int pageSize;
    
    /**
     * Constructor
     * @param username The username to query
     * @param cursorString The cursor string for pagination (null for first page)
     * @param pageSize The number of records per page (1-100)
     */
    public QueryRecentExpendituresCommand(String username, String cursorString, int pageSize) {
        // Validation
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100, got: " + pageSize);
        }
        
        this.username = username.trim();
        this.cursorString = cursorString != null && cursorString.trim().isEmpty() ? null : cursorString;
        this.pageSize = pageSize;
    }
    
    /**
     * Constructor with default page size (20)
     * @param username The username to query
     * @param cursorString The cursor string for pagination (null for first page)
     */
    public QueryRecentExpendituresCommand(String username, String cursorString) {
        this(username, cursorString, 20);
    }
    
    /**
     * Constructor for first page with default page size
     * @param username The username to query
     */
    public QueryRecentExpendituresCommand(String username) {
        this(username, null, 20);
    }
    
    /**
     * Get the username
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get the cursor string
     * @return cursor string or null for first page
     */
    public String getCursorString() {
        return cursorString;
    }
    
    /**
     * Get the page size
     * @return page size
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Check if this is the first page
     * @return true if cursor is null
     */
    public boolean isFirstPage() {
        return cursorString == null;
    }
    
    @Override
    public String toString() {
        return "QueryRecentExpendituresCommand{" +
                "username='" + username + '\'' +
                ", cursorString='" + (cursorString != null ? "***" : "null") + '\'' +
                ", pageSize=" + pageSize +
                '}';
    }
}
