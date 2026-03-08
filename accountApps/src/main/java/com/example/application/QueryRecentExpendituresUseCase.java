package com.example.application;

import com.example.application.command.QueryRecentExpendituresCommand;
import com.example.application.exception.InvalidCursorException;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.valueobject.Cursor;
import com.example.domain.valueobject.PageResult;

/**
 * Use case for querying recent expenditures with cursor-based pagination
 */
public class QueryRecentExpendituresUseCase {
    private final ExpenditureRecordRepository expenditureRecordRepository;
    private final UserRepository userRepository;
    
    /**
     * Constructor
     * @param expenditureRecordRepository The expenditure record repository
     * @param userRepository The user repository
     */
    public QueryRecentExpendituresUseCase(
            ExpenditureRecordRepository expenditureRecordRepository,
            UserRepository userRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Execute the query with cursor-based pagination
     * @param command The query command
     * @return PageResult containing expenditure records
     * @throws IllegalArgumentException if user not found
     * @throws InvalidCursorException if cursor is invalid
     */
    public PageResult<ExpenditureRecord> execute(QueryRecentExpendituresCommand command) {
        // 1. Load user
        User user = userRepository.findByUsername(command.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.getUsername()));
        
        // 2. Decode cursor if present
        Cursor cursor = null;
        if (!command.isFirstPage()) {
            try {
                cursor = Cursor.decode(command.getCursorString());
            } catch (InvalidCursorException e) {
                throw new InvalidCursorException(
                    "Invalid cursor for user " + command.getUsername() + ": " + e.getMessage(), e);
            }
        }
        
        // 3. Query with cursor-based pagination
        PageResult<ExpenditureRecord> result = expenditureRecordRepository.findRecentByUserWithCursor(
            user, cursor, command.getPageSize()
        );
        
        return result;
    }
    
    /**
     * Execute the query with offset-based pagination (for performance comparison)
     * @param command The query command
     * @param offset The offset (number of records to skip)
     * @return PageResult containing expenditure records
     * @throws IllegalArgumentException if user not found
     */
    public PageResult<ExpenditureRecord> executeWithOffset(
            QueryRecentExpendituresCommand command, int offset) {
        
        // 1. Load user
        User user = userRepository.findByUsername(command.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.getUsername()));
        
        // 2. Query with offset-based pagination
        PageResult<ExpenditureRecord> result = expenditureRecordRepository.findRecentByUserWithOffset(
            user, offset, command.getPageSize()
        );
        
        return result;
    }
}
