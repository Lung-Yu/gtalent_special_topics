package com.example.infrastructure.persistence;

import com.example.domain.repository.ExpenditureRecordRepository;
import com.example.domain.model.ExpenditureRecord;
import com.example.domain.model.StatisticsPoint;
import com.example.domain.model.User;
import com.example.domain.valueobject.Cursor;
import com.example.domain.valueobject.PageResult;
import com.example.domain.valueobject.StatisticsCategory;
import com.example.domain.valueobject.UserIdentity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryExpenditureRecordRepository implements ExpenditureRecordRepository {
    private List<ExpenditureRecord> records = new ArrayList<>();
    
    @Override
    public List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date) {
        return records.stream()
                .filter(record -> record.getUsername().equals(user.getUsername()) && 
                         record.getDate().equals(date))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExpenditureRecord> findByDate(LocalDate date) {
        return records.stream()
                .filter(record -> record.getDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenditureRecord> findByUser(User user) {
        return records.stream()
                .filter(record -> record.getUsername().equals(user.getUsername()))
                .collect(Collectors.toList());
    }
    
    @Override
    public void save(ExpenditureRecord record) {
        records.add(record);
    }
    
    @Override
    public List<ExpenditureRecord> findAll() {
        return new ArrayList<>(records);
    }
    
    @Override
    public List<StatisticsPoint> findStatisticsByUserAndDate(User user, LocalDate date) {
        if (user == null || date == null) {
            return new ArrayList<>();
        }
        
        // 過濾特定使用者和日期的記錄
        List<ExpenditureRecord> filteredRecords = records.stream()
                .filter(record -> record.getUsername().equals(user.getUsername()) && 
                                  record.getDate().equals(date))
                .collect(Collectors.toList());
        
        // 按分類聚合金額
        Map<String, Integer> categoryAmountMap = new HashMap<>();
        
        for (ExpenditureRecord record : filteredRecords) {
            for (String categoryName : record.getCategory()) {
                categoryAmountMap.merge(categoryName, record.getMoney(), Integer::sum);
            }
        }
        
        // 轉換為 StatisticsPoint 列表
        List<StatisticsPoint> points = new ArrayList<>();
        LocalDateTime time = date.atStartOfDay();
        
        for (Map.Entry<String, Integer> entry : categoryAmountMap.entrySet()) {
            try {
                StatisticsCategory category = StatisticsCategory.valueOf(entry.getKey());
                UserIdentity userIdentity = UserIdentity.from(user);
                StatisticsPoint point = new StatisticsPoint(
                    entry.getValue(), userIdentity, time, category
                );
                points.add(point);
            } catch (IllegalArgumentException e) {
                // 分類名稱不在 enum 中，忽略該筆資料
                System.err.println("警告: 未知的統計分類 '" + entry.getKey() + 
                                 "'，已略過 (使用者: " + user.getUsername() + ")");
            }
        }
        
        return points;
    }
    
    @Override
    public List<StatisticsPoint> findStatisticsByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        // 過濾特定日期的記錄
        List<ExpenditureRecord> filteredRecords = records.stream()
                .filter(record -> record.getDate().equals(date))
                .collect(Collectors.toList());
        
        // 按使用者和分類聚合金額
        // Key: username + "_" + categoryName, Value: 金額總和
        Map<String, UserCategoryAmount> aggregationMap = new HashMap<>();
        
        for (ExpenditureRecord record : filteredRecords) {
            UserIdentity userIdentity = record.getUserIdentity();
            for (String categoryName : record.getCategory()) {
                String key = userIdentity.getUsername() + "_" + categoryName;
                
                if (aggregationMap.containsKey(key)) {
                    UserCategoryAmount existing = aggregationMap.get(key);
                    existing.amount += record.getMoney();
                } else {
                    aggregationMap.put(key, new UserCategoryAmount(
                        userIdentity, categoryName, record.getMoney()
                    ));
                }
            }
        }
        
        // 轉換為 StatisticsPoint 列表
        List<StatisticsPoint> points = new ArrayList<>();
        LocalDateTime time = date.atStartOfDay();
        
        for (UserCategoryAmount uca : aggregationMap.values()) {
            try {
                StatisticsCategory category = StatisticsCategory.valueOf(uca.categoryName);
                StatisticsPoint point = new StatisticsPoint(
                    uca.amount, uca.userIdentity, time, category
                );
                points.add(point);
            } catch (IllegalArgumentException e) {
                // 分類名稱不在 enum 中，忽略該筆資料
                System.err.println("警告: 未知的統計分類 '" + uca.categoryName + 
                                 "'，已略過 (使用者: " + uca.userIdentity.getUsername() + ")");
            }
        }
        
        return points;
    }
    
    @Override
    public List<StatisticsPoint> findStatisticsByCategoryAndDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        // 過濾特定日期的記錄
        List<ExpenditureRecord> filteredRecords = records.stream()
                .filter(record -> record.getDate().equals(date))
                .collect(Collectors.toList());
        
        // 只按分類聚合金額，不區分使用者
        Map<String, Integer> categoryAmountMap = new HashMap<>();
        
        for (ExpenditureRecord record : filteredRecords) {
            for (String categoryName : record.getCategory()) {
                categoryAmountMap.merge(categoryName, record.getMoney(), Integer::sum);
            }
        }
        
        // 轉換為 StatisticsPoint 列表
        List<StatisticsPoint> points = new ArrayList<>();
        LocalDateTime time = date.atStartOfDay();
        
        for (Map.Entry<String, Integer> entry : categoryAmountMap.entrySet()) {
            try {
                StatisticsCategory category = StatisticsCategory.valueOf(entry.getKey());
                // userIdentity 設為 null 表示不分使用者的統計
                StatisticsPoint point = new StatisticsPoint(
                    entry.getValue(), (UserIdentity) null, time, category
                );
                points.add(point);
            } catch (IllegalArgumentException e) {
                // 分類名稱不在 enum 中，忽略該筆資料
                System.err.println("警告: 未知的統計分類 '" + entry.getKey() + "'，已略過");
            }
        }
        
        return points;
    }
    
    /**
     * 內部類別，用於儲存使用者身份、分類和金額的聚合資訊
     */
    private static class UserCategoryAmount {
        UserIdentity userIdentity;
        String categoryName;
        int amount;
        
        UserCategoryAmount(UserIdentity userIdentity, String categoryName, int amount) {
            this.userIdentity = userIdentity;
            this.categoryName = categoryName;
            this.amount = amount;
        }
    }
    
    @Override
    public PageResult<ExpenditureRecord> findRecentByUserWithCursor(User user, Cursor cursor, int limit) {
        if (user == null) {
            return new PageResult<>(new ArrayList<>());
        }
        
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100, got: " + limit);
        }
        
        // 建立記錄與索引的映射（索引作為 ID）
        List<RecordWithId> recordsWithId = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            ExpenditureRecord record = records.get(i);
            if (record.getUsername().equals(user.getUsername())) {
                recordsWithId.add(new RecordWithId((long) i, record));
            }
        }
        
        // 排序：date DESC, id DESC
        recordsWithId.sort(Comparator
            .comparing((RecordWithId r) -> r.record.getDate()).reversed()
            .thenComparing((RecordWithId r) -> r.id, Comparator.reverseOrder()));
        
        // 應用游標過濾
        List<RecordWithId> filtered;
        if (cursor != null) {
            filtered = recordsWithId.stream()
                .filter(r -> {
                    LocalDate recordDate = r.record.getDate();
                    // (date < cursor_date) OR (date = cursor_date AND id < cursor_id)
                    return recordDate.isBefore(cursor.getDate()) ||
                           (recordDate.isEqual(cursor.getDate()) && r.id < cursor.getId());
                })
                .collect(Collectors.toList());
        } else {
            filtered = recordsWithId;
        }
        
        // 取得 limit + 1 筆資料以判斷是否有下一頁
        boolean hasMore = filtered.size() > limit;
        List<RecordWithId> page = filtered.stream()
            .limit(limit + 1)
            .collect(Collectors.toList());
        
        if (hasMore) {
            page = page.subList(0, limit);
        }
        
        // 建立下一頁的游標
        Cursor nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            RecordWithId lastRecord = page.get(page.size() - 1);
            nextCursor = new Cursor(lastRecord.record.getDate(), lastRecord.id);
        }
        
        // 只返回記錄，不包含 ID
        List<ExpenditureRecord> result = page.stream()
            .map(r -> r.record)
            .collect(Collectors.toList());
        
        return new PageResult<>(result, nextCursor, hasMore);
    }
    
    @Override
    public PageResult<ExpenditureRecord> findRecentByUserWithOffset(User user, int offset, int limit) {
        if (user == null) {
            return new PageResult<>(new ArrayList<>());
        }
        
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative, got: " + offset);
        }
        
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100, got: " + limit);
        }
        
        // 建立記錄與索引的映射
        List<RecordWithId> recordsWithId = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            ExpenditureRecord record = records.get(i);
            if (record.getUsername().equals(user.getUsername())) {
                recordsWithId.add(new RecordWithId((long) i, record));
            }
        }
        
        // 排序：date DESC, id DESC
        recordsWithId.sort(Comparator
            .comparing((RecordWithId r) -> r.record.getDate()).reversed()
            .thenComparing((RecordWithId r) -> r.id, Comparator.reverseOrder()));
        
        // 應用 OFFSET 和 LIMIT
        List<RecordWithId> page = recordsWithId.stream()
            .skip(offset)
            .limit(limit + 1)  // 取 limit + 1 以判斷是否有下一頁
            .collect(Collectors.toList());
        
        // 判斷是否有下一頁
        boolean hasMore = page.size() > limit;
        if (hasMore) {
            page = page.subList(0, limit);
        }
        
        // 建立下一頁的游標
        Cursor nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            RecordWithId lastRecord = page.get(page.size() - 1);
            nextCursor = new Cursor(lastRecord.record.getDate(), lastRecord.id);
        }
        
        // 只返回記錄，不包含 ID
        List<ExpenditureRecord> result = page.stream()
            .map(r -> r.record)
            .collect(Collectors.toList());
        
        return new PageResult<>(result, nextCursor, hasMore);
    }
    
    /**
     * 內部類別，將記錄與 ID 配對（用於分頁）
     */
    private static class RecordWithId {
        long id;
        ExpenditureRecord record;
        
        RecordWithId(long id, ExpenditureRecord record) {
            this.id = id;
            this.record = record;
        }
    }
}
