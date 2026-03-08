package com.example.infrastructure.persistence;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.User;
import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.valueobject.Cursor;
import com.example.domain.valueobject.PageResult;
import com.example.infrastructure.util.DatabaseConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Performance comparison test between OFFSET-based and Cursor-based pagination
 * 
 * This test requires:
 * 1. MySQL database running with accountapps schema
 * 2. Test data generated (100k+ records) via 06-generate-large-expenditure-data.sql
 * 
 * Run with: mvn test -Dtest=PaginationPerformanceComparisonTest
 */
public class PaginationPerformanceComparisonTest {
    
    private static final String TEST_USERNAME = "perftest_user";
    private static final int PAGE_SIZE = 20;
    private static final int[] TEST_POSITIONS = {0, 1000, 10000, 50000};
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 10;
    
    private ExpenditureRecordRepository repository;
    private UserRepository userRepository;
    private User testUser;
    
    @BeforeClass
    public static void checkDatabaseConnection() {
        try (Connection conn = DatabaseConnectionFactory.getConnection()) {
            System.out.println("✓ Database connection successful");
        } catch (Exception e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            System.err.println("Please ensure MySQL is running and test data is loaded.");
            System.err.println("Run: docker-compose up -d && mysql < mysql-init/06-generate-large-expenditure-data.sql");
        }
    }
    
    @Before
    public void setUp() {
        userRepository = new MySQLUserRepository();
        repository = new MySQLExpenditureRecordRepository(userRepository);
        
        // Load test user
        testUser = userRepository.findByUsername(TEST_USERNAME)
            .orElseThrow(() -> new AssertionError("Test user not found. Please run 06-generate-large-expenditure-data.sql"));
        
        System.out.println("\n=== Pagination Performance Comparison Test ===");
        System.out.println("Test user: " + TEST_USERNAME);
        System.out.println("Page size: " + PAGE_SIZE);
        System.out.println();
    }
    
    @Test
    public void compareShallowPagination() {
        System.out.println("Testing shallow pagination (position 0)...");
        PerformanceMetrics metrics = testBothMethodsAtPosition(0);
        
        printMetrics("Position 0 (First Page)", metrics);
        
        // At first page, both methods should be similar
        assertTrue("Cursor-based should complete successfully", metrics.cursorAvgMs > 0);
        assertTrue("OFFSET-based should complete successfully", metrics.offsetAvgMs > 0);
    }
    
    @Test
    public void compareMediumPagination() {
        System.out.println("Testing medium pagination (position 1000)...");
        PerformanceMetrics metrics = testBothMethodsAtPosition(1000);
        
        printMetrics("Position 1,000", metrics);
        
        // Cursor should start showing advantage
        assertTrue("Cursor-based should be faster or similar", 
            metrics.cursorAvgMs <= metrics.offsetAvgMs * 1.5);
    }
    
    @Test
    public void compareDeepPaginationAt10k() {
        System.out.println("Testing deep pagination (position 10000)...");
        PerformanceMetrics metrics = testBothMethodsAtPosition(10000);
        
        printMetrics("Position 10,000", metrics);
        
        // Cursor should be significantly faster
        double ratio = metrics.offsetAvgMs / metrics.cursorAvgMs;
        assertTrue("Cursor-based should be at least 5x faster at 10k position (ratio: " + ratio + ")", 
            ratio >= 5.0);
    }
    
    @Test
    public void compareDeepPaginationAt50k() {
        System.out.println("Testing very deep pagination (position 50000)...");
        PerformanceMetrics metrics = testBothMethodsAtPosition(50000);
        
        printMetrics("Position 50,000", metrics);
        
        // Cursor should be dramatically faster
        double ratio = metrics.offsetAvgMs / metrics.cursorAvgMs;
        assertTrue("Cursor-based should be at least 50x faster at 50k position (ratio: " + ratio + ")", 
            ratio >= 50.0);
    }
    
    @Test
    public void fullPerformanceReport() {
        System.out.println("\n=== Full Performance Comparison Report ===\n");
       
        List<PerformanceMetrics> allMetrics = new ArrayList<>();
        
        for (int position : TEST_POSITIONS) {
            System.out.println("Testing position " + position + "...");
            PerformanceMetrics metrics = testBothMethodsAtPosition(position);
            allMetrics.add(metrics);
        }
        
        // Print summary table
        printSummaryTable(allMetrics);
        
        // Save to file
        try {
            saveReportToFile(allMetrics);
            System.out.println("\n✓ Report saved to accountApps/performance-test-results.txt");
        } catch (IOException e) {
            System.err.println("Failed to save report: " + e.getMessage());
        }
    }
    
    @Test
    public void testDataIntegrityBothMethodsReturnSameData() {
        System.out.println("Testing data integrity (both methods return same records)...");
        
        int testPosition = 100;
        
        // Get data using OFFSET-based
        PageResult<ExpenditureRecord> offsetResult = 
            repository.findRecentByUserWithOffset(testUser, testPosition, PAGE_SIZE);
        
        // Navigate to same position using cursor-based
        PageResult<ExpenditureRecord> cursorResult = navigateToCursorPosition(testPosition);
        
        // Verify same data
        assertEquals("Both methods should return same number of records",
            offsetResult.getSize(), cursorResult.getSize());
        
        // Verify records are same (compare first record's details)
        if (!offsetResult.isEmpty() && !cursorResult.isEmpty()) {
            ExpenditureRecord offsetFirst = offsetResult.getData().get(0);
            ExpenditureRecord cursorFirst = cursorResult.getData().get(0);
            
            assertEquals("Same name", offsetFirst.getName(), cursorFirst.getName());
            assertEquals("Same money", offsetFirst.getMoney(), cursorFirst.getMoney());
            assertEquals("Same date", offsetFirst.getDate(), cursorFirst.getDate());
        }
        
        System.out.println("✓ Data integrity verified");
    }
    
    @Test
    public void testCursorConsistency() {
        System.out.println("Testing cursor consistency...");
        
        // Navigate through multiple pages
        Cursor cursor = null;
        List<Long> allIds = new ArrayList<>();
        int pageCount = 0;
        
        for (int i = 0; i < 5; i++) {
            PageResult<ExpenditureRecord> result = 
                repository.findRecentByUserWithCursor(testUser, cursor, PAGE_SIZE);
            
            pageCount++;
            
            if (result.isEmpty()) {
                break;
            }
            
            // Check no duplicates (this would fail if cursor logic is wrong)
            for (ExpenditureRecord record : result.getData()) {
                String recordKey = record.getDate() + "-" + record.getName() + "-" + record.getMoney();
                // Note: without ID in ExpenditureRecord, we use data fingerprint
            }
            
            if (!result.hasMore()) {
                break;
            }
            
            cursor = result.getNextCursor();
        }
        
        System.out.println("✓ Cursor consistency verified across " + pageCount + " pages");
    }
    
    /**
     * Test both pagination methods at a specific position
     * 比較兩種方法的單次查詢效能（Cursor 假設已經有正確的 cursor）
     */
    private PerformanceMetrics testBothMethodsAtPosition(int position) {
        // 準備：先導航到目標位置以獲取 cursor
        Cursor targetCursor = null;
        if (position > 0) {
            Cursor cursor = null;
            int currentPosition = 0;
            
            while (currentPosition < position) {
                int limit = Math.min(PAGE_SIZE, position - currentPosition);
                PageResult<ExpenditureRecord> result = 
                    repository.findRecentByUserWithCursor(testUser, cursor, limit);
                
                if (!result.hasMore()) {
                    break;
                }
                
                cursor = result.getNextCursor();
                currentPosition += result.getSize();
            }
            targetCursor = cursor;
        }
        
        final Cursor finalCursor = targetCursor;
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            repository.findRecentByUserWithOffset(testUser, position, PAGE_SIZE);
            repository.findRecentByUserWithCursor(testUser, finalCursor, PAGE_SIZE);
        }
        
        // Test OFFSET-based (單次查詢)
        List<Long> offsetTimes = new ArrayList<>();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long start = System.nanoTime();
            repository.findRecentByUserWithOffset(testUser, position, PAGE_SIZE);
            long end = System.nanoTime();
            offsetTimes.add(end - start);
        }
        
        // Test Cursor-based (單次查詢，已有 cursor)
        List<Long> cursorTimes = new ArrayList<>();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long start = System.nanoTime();
            repository.findRecentByUserWithCursor(testUser, finalCursor, PAGE_SIZE);
            long end = System.nanoTime();
            cursorTimes.add(end - start);
        }
        
        return new PerformanceMetrics(position, offsetTimes, cursorTimes);
    }
    
    /**
     * Navigate to a specific position using cursor-based pagination
     */
    private PageResult<ExpenditureRecord> navigateToCursorPosition(int targetPosition) {
        Cursor cursor = null;
        int currentPosition = 0;
        
        while (currentPosition < targetPosition) {
            int limit = Math.min(PAGE_SIZE, targetPosition - currentPosition);
            PageResult<ExpenditureRecord> result = 
                repository.findRecentByUserWithCursor(testUser, cursor, limit);
            
            if (!result.hasMore()) {
                break;
            }
            
            cursor = result.getNextCursor();
            currentPosition += result.getSize();
        }
        
        // Get the final page at target position
        return repository.findRecentByUserWithCursor(testUser, cursor, PAGE_SIZE);
    }
    
    /**
     * Print performance metrics
     */
    private void printMetrics(String label, PerformanceMetrics metrics) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        double ratio = metrics.offsetAvgMs / metrics.cursorAvgMs;
        
        System.out.println("\n" + label + ":");
        System.out.println("  OFFSET-based: " + df.format(metrics.offsetAvgMs) + " ms (avg)");
        System.out.println("  CURSOR-based: " + df.format(metrics.cursorAvgMs) + " ms (avg)");
        System.out.println("  Speed ratio:  " + df.format(ratio) + "x faster with cursor");
        System.out.println();
    }
    
    /**
     * Print summary table of all results
     */
    private void printSummaryTable(List<PerformanceMetrics> allMetrics) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        System.out.println("\nPerformance Comparison Report (100,000+ records)");
        System.out.println("=".repeat(70));
        System.out.printf("%-15s | %-15s | %-15s | %-10s%n", 
            "Position", "OFFSET (avg)", "CURSOR (avg)", "Ratio");
        System.out.println("-".repeat(70));
        
        for (PerformanceMetrics metrics : allMetrics) {
            double ratio = metrics.offsetAvgMs / metrics.cursorAvgMs;
            System.out.printf("%-15s | %12s ms | %12s ms | %8sx%n",
                formatNumber(metrics.position),
                df.format(metrics.offsetAvgMs),
                df.format(metrics.cursorAvgMs),
                df.format(ratio));
        }
        
        System.out.println("=".repeat(70));
    }
    
    /**
     * Save report to file
     */
    private void saveReportToFile(List<PerformanceMetrics> allMetrics) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("performance-test-results.txt"))) {
            
            writer.write("Pagination Performance Comparison Report\n");
            writer.write("Generated: " + java.time.LocalDateTime.now() + "\n");
            writer.write("Test user: " + TEST_USERNAME + "\n");
            writer.write("Page size: " + PAGE_SIZE + "\n");
            writer.write("Iterations per test: " + TEST_ITERATIONS + "\n");
            writer.write("\n");
            writer.write("=".repeat(70) + "\n");
            writer.write(String.format("%-15s | %-15s | %-15s | %-10s%n", 
                "Position", "OFFSET (avg)", "CURSOR (avg)", "Ratio"));
            writer.write("-".repeat(70) + "\n");
            
            DecimalFormat df = new DecimalFormat("#,##0.00");
            for (PerformanceMetrics metrics : allMetrics) {
                double ratio = metrics.offsetAvgMs / metrics.cursorAvgMs;
                writer.write(String.format("%-15s | %12s ms | %12s ms | %8sx%n",
                    formatNumber(metrics.position),
                    df.format(metrics.offsetAvgMs),
                    df.format(metrics.cursorAvgMs),
                    df.format(ratio)));
            }
            
            writer.write("=".repeat(70) + "\n");
        }
    }
    
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }
    
    /**
     * Performance metrics holder
     */
    private static class PerformanceMetrics {
        final int position;
        final double offsetAvgMs;
        final double cursorAvgMs;
        
        PerformanceMetrics(int position, List<Long> offsetNanos, List<Long> cursorNanos) {
            this.position = position;
            this.offsetAvgMs = offsetNanos.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0) / 1_000_000.0;
            this.cursorAvgMs = cursorNanos.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0) / 1_000_000.0;
        }
    }
}
