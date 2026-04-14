package com.example.infrastructure.persistence;

import java.util.HashMap;
import java.util.Map;

import com.example.domain.model.Inventory;
import com.example.domain.repository.InventoryRepository;

public class HighInventoryRepository implements InventoryRepository{

    // 使用兩層 HashMap: 外層用前綴(2個英文字母)，內層用完整ID
    private Map<String, Map<String, Inventory>> inventoryMap;

    public HighInventoryRepository(){
        this.inventoryMap = new HashMap<>();
    }

    @Override
    public Inventory findById(String id) {
        // Input validation: 驗證 ID 格式 (2個字母 + 6位數字)
        if (id == null || id.length() != 8) {
            return null;
        }
        
        // 檢查前兩個字符是否為字母
        if (!Character.isLetter(id.charAt(0)) || !Character.isLetter(id.charAt(1))) {
            return null;
        }
        
        // 檢查後六個字符是否為數字
        for (int i = 2; i < 8; i++) {
            if (!Character.isDigit(id.charAt(i))) {
                return null;
            }
        }
        
        // 提取前兩個英文字母作為前綴
        String prefix = id.substring(0, 2);
        
        // 先透過前綴快速篩選，取得該前綴的子 Map
        Map<String, Inventory> subMap = inventoryMap.get(prefix);
        if (subMap == null) {
            return null;
        }
        
        // 再用完整 ID 在子 Map 中查詢
        return subMap.get(id);
    }

    @Override
    public void save(Inventory inventory) {
        String id = inventory.getId();
        if (id == null || id.length() < 2) {
            return;
        }
        
        // 提取前兩個英文字母作為前綴
        String prefix = id.substring(0, 2);
        
        // 如果該前綴的子 Map 不存在，則建立一個
        Map<String, Inventory> subMap = inventoryMap.computeIfAbsent(prefix, k -> new HashMap<>());
        
        // 將 Inventory 存入子 Map
        subMap.put(id, inventory);
    }

}
