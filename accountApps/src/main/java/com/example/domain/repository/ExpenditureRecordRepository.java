package com.example.domain.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.model.User;
import com.example.domain.valueobject.Cursor;
import com.example.domain.valueobject.PageResult;

public interface ExpenditureRecordRepository {
    List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date);
    
    List<ExpenditureRecord> findByDate(LocalDate date);

    List<ExpenditureRecord> findByUser(User user);

    void save(ExpenditureRecord record);

    List<ExpenditureRecord> findAll();
    
    /**
     * 查詢特定使用者在特定日期的支出統計（按分類聚合）
     * 此方法在資料庫層進行聚合計算，避免在記憶體中處理大量資料
     * 
     * @param user 使用者
     * @param date 查詢日期
     * @return 按分類聚合的統計點列表
     */
    List<StatisticsPoint> findStatisticsByUserAndDate(User user, LocalDate date);
    
    /**
     * 查詢特定日期所有使用者的支出統計（按使用者和分類聚合）
     * 此方法在資料庫層進行聚合計算，避免 N+1 查詢問題
     * 
     * @param date 查詢日期
     * @return 按使用者和分類聚合的統計點列表
     */
    List<StatisticsPoint> findStatisticsByDate(LocalDate date);
    
    /**
     * 查詢特定日期所有使用者的支出統計（僅按分類聚合，不分使用者）
     * 此方法用於管理者視角的統計，將所有使用者的同類別支出合併
     * 
     * @param date 查詢日期
     * @return 按分類聚合的統計點列表（user 設為 null）
     */
    List<StatisticsPoint> findStatisticsByCategoryAndDate(LocalDate date);
    
    /**
     * 使用 Cursor-based pagination 查詢特定使用者的最近支出記錄
     * 此方法提供高效能的分頁查詢，適用於大量資料場景
     * 
     * @param user 使用者
     * @param cursor 游標（null 表示第一頁）
     * @param limit 每頁筆數（建議 1-100）
     * @return 分頁結果，包含資料、下一頁游標和是否有更多資料
     */
    PageResult<ExpenditureRecord> findRecentByUserWithCursor(User user, Cursor cursor, int limit);
    
    /**
     * 使用 OFFSET-based pagination 查詢特定使用者的最近支出記錄
     * 此方法用於效能比較基準，不建議在生產環境用於深度分頁
     * 
     * @param user 使用者
     * @param offset 跳過的筆數
     * @param limit 每頁筆數
     * @return 分頁結果，包含資料、下一頁游標和是否有更多資料
     */
    PageResult<ExpenditureRecord> findRecentByUserWithOffset(User user, int offset, int limit);
}
