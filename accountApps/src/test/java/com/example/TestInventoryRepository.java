package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.example.domain.model.Inventory;
import com.example.domain.repository.InventoryRepository;
import com.example.infrastructure.persistence.LowInventoryRepository;

public class TestInventoryRepository {

    private InventoryRepository inventoryRepository;

    @Before
    public void setUp() {
        inventoryRepository = new LowInventoryRepository();

        // Generate all combinations using loops - 增加到 1000 筆資料
        String[] prefixes = { "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO",
                "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ" };
        int testCounter = 1;

        for (String prefix : prefixes) {
            for (int i = 1; i <= 100; i++) {
                String id = prefix + String.format("%03d", i);
                String name = "test" + String.format("%04d", testCounter);
                inventoryRepository.save(new Inventory(id, name));
                testCounter++;
                if (testCounter > 1000)
                    break; // Stop at test1000
            }
            if (testCounter > 1000)
                break;
        }

    }

    @Test
    public void testLow() {
        // 使用奈秒級計時並執行多次查詢
        int iterations = 1000;
        
        // 單次查詢測試 - 查詢最後一筆資料 AJ100 (test1000)
        long startNano = System.nanoTime();
        Inventory inventory = inventoryRepository.findById("AJ100");
        long endNano = System.nanoTime();
        
        assertEquals("test1000", inventory.getName());
        
        long singleDuration = endNano - startNano;
        System.out.println("單次查詢執行時間: " + singleDuration + " 奈秒 (" + (singleDuration / 1000000.0) + " 毫秒)");
        
        // 多次查詢測試
        startNano = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            inventoryRepository.findById("AJ100");
        }
        endNano = System.nanoTime();
        
        long totalDuration = endNano - startNano;
        double avgDuration = totalDuration / (double) iterations;
        System.out.println(iterations + " 次查詢總執行時間: " + totalDuration + " 奈秒 (" + (totalDuration / 1000000.0) + " 毫秒)");
        System.out.println("平均單次查詢時間: " + String.format("%.2f", avgDuration) + " 奈秒 (" + String.format("%.6f", avgDuration / 1000000.0) + " 毫秒)");
    }

}
