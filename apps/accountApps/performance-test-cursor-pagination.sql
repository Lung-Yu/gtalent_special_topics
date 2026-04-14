-- ========================================
-- Performance Comparison: OFFSET vs CURSOR-based Pagination
-- ========================================
-- 
-- This script demonstrates the performance difference between
-- OFFSET-based and Cursor-based pagination on large datasets.
-- 
-- Prerequisites:
-- 1. Run 06-generate-large-expenditure-data.sql to create test data
-- 2. Ensure idx_username_date_id_desc index exists
--
-- ========================================

USE accountapps;

-- Check data exists
SELECT 
    'Test Data Status' AS Check_Type,
    COUNT(*) AS TotalRecords,
    COUNT(DISTINCT username) AS TotalUsers,
    MIN(date) AS EarliestDate,
    MAX(date) AS LatestDate
FROM expenditure_records;

-- Check indexes
SELECT 
    'Index Status' AS Check_Type,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    COLLATION
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'accountapps'
  AND TABLE_NAME = 'expenditure_records'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- ========================================
-- Test 1: First Page (Position 0)
-- ========================================

SELECT '\n=== Test 1: First Page (Position 0) ===' AS Test;

-- OFFSET-based (baseline)
EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
ORDER BY date DESC, id DESC
LIMIT 20 OFFSET 0;

-- Cursor-based
EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
ORDER BY date DESC, id DESC
LIMIT 20;

-- ========================================
-- Test 2: Medium Depth (Position ~1000)
-- ========================================

SELECT '\n=== Test 2: Medium Depth (Position ~1000) ===' AS Test;

-- OFFSET-based (degrading performance)
EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
ORDER BY date DESC, id DESC
LIMIT 20 OFFSET 1000;

-- Cursor-based (using sample cursor values - replace with actual values from your data)
-- To get actual cursor values, first run:
-- SELECT id, date FROM expenditure_records 
-- WHERE username = 'perftest_user' 
-- ORDER BY date DESC, id DESC 
-- LIMIT 1 OFFSET 999;

SET @cursor_date_1k = (
    SELECT date FROM expenditure_records 
    WHERE username = 'perftest_user' 
    ORDER BY date DESC, id DESC 
    LIMIT 1 OFFSET 999
);

SET @cursor_id_1k = (
    SELECT id FROM expenditure_records 
    WHERE username = 'perftest_user' 
    ORDER BY date DESC, id DESC 
    LIMIT 1 OFFSET 999
);

EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
  AND (date < @cursor_date_1k OR (date = @cursor_date_1k AND id < @cursor_id_1k))
ORDER BY date DESC, id DESC
LIMIT 20;

-- ========================================
-- Test 3: Deep Pagination (Position ~10000)
-- ========================================

SELECT '\n=== Test 3: Deep Pagination (Position ~10000) ===' AS Test;

-- OFFSET-based (very slow)
EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
ORDER BY date DESC, id DESC
LIMIT 20 OFFSET 10000;

-- Cursor-based (still fast)
SET @cursor_date_10k = (
    SELECT date FROM expenditure_records 
    WHERE username = 'perftest_user' 
    ORDER BY date DESC, id DESC 
    LIMIT 1 OFFSET 9999
);

SET @cursor_id_10k = (
    SELECT id FROM expenditure_records 
    WHERE username = 'perftest_user' 
    ORDER BY date DESC, id DESC 
    LIMIT 1 OFFSET 9999
);

EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
  AND (date < @cursor_date_10k OR (date = @cursor_date_10k AND id < @cursor_id_10k))
ORDER BY date DESC, id DESC
LIMIT 20;

-- ========================================
-- Test 4: Very Deep Pagination (Position ~50000)
-- ========================================

SELECT '\n=== Test 4: Very Deep Pagination (Position ~50000) ===' AS Test;

-- OFFSET-based (extremely slow)
EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
ORDER BY date DESC, id DESC
LIMIT 20 OFFSET 50000;

-- Cursor-based (consistently fast)
SET @cursor_date_50k = (
    SELECT date FROM expenditure_records 
    WHERE username = 'perftest_user' 
    ORDER BY date DESC, id DESC 
    LIMIT 1 OFFSET 49999
);

SET @cursor_id_50k = (
    SELECT id FROM expenditure_records 
    WHERE username = 'perftest_user' 
    ORDER BY date DESC, id DESC 
    LIMIT 1 OFFSET 49999
);

EXPLAIN ANALYZE
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
  AND (date < @cursor_date_50k OR (date = @cursor_date_50k AND id < @cursor_id_50k))
ORDER BY date DESC, id DESC
LIMIT 20;

-- ========================================
-- Expected Results Summary
-- ========================================
/*
Expected Performance Characteristics:

Position 0:
- OFFSET: ~5-10ms (using index)
- CURSOR: ~5-10ms (using index)
- Ratio: ~1x (similar performance)

Position 1,000:
- OFFSET: ~50-100ms (must skip 1000 rows)
- CURSOR: ~5-10ms (direct index lookup)
- Ratio: ~10x faster with cursor

Position 10,000:
- OFFSET: ~500-1000ms (must skip 10000 rows)
- CURSOR: ~5-10ms (direct index lookup)
- Ratio: ~100x faster with cursor

Position 50,000:
- OFFSET: ~2500-5000ms (must skip 50000 rows)
- CURSOR: ~5-10ms (direct index lookup)
- Ratio: ~500x faster with cursor

Key Insights:
1. OFFSET performance degrades linearly with position
2. CURSOR performance remains constant regardless of position
3. The idx_username_date_id_desc index is crucial for both methods
4. Cursor-based pagination becomes dramatically better for deep pagination
5. Memory usage is also better with cursor (no need to skip rows)
*/

-- ========================================
-- Verify Index Usage
-- ========================================

SELECT '\n=== Verify Index Usage ===' AS Test;

-- This should show "Using index" in Extra column
EXPLAIN
SELECT * FROM expenditure_records
WHERE username = 'perftest_user'
  AND (date < '2025-01-01' OR (date = '2025-01-01' AND id < 50000))
ORDER BY date DESC, id DESC
LIMIT 20;

-- Check if covering index would help (shows current index coverage)
SHOW INDEX FROM expenditure_records WHERE Key_name = 'idx_username_date_id_desc';

SELECT 'Performance test completed!' AS Status;
