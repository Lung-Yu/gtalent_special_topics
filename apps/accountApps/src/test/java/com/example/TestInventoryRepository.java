package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.example.domain.model.Inventory;
import com.example.domain.repository.InventoryRepository;
import com.example.infrastructure.persistence.HighInventoryRepository;
import com.example.infrastructure.persistence.LowInventoryRepository;

public class TestInventoryRepository {

    private InventoryRepository lowInventoryRepository;

    private InventoryRepository highInventoryRepository;
    @Before
    public void setUp() {
        lowInventoryRepository = new LowInventoryRepository();
        highInventoryRepository = new HighInventoryRepository();

        initTestData(highInventoryRepository);
        initTestData(lowInventoryRepository);
    }

    private void initTestData(InventoryRepository inventoryRepository) {
        // Generate all combinations using loops - 每組前綴10萬筆資料，共26組 = 260萬筆
        String[] prefixes = { "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO",
                "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ" };
        int testCounter = 1;

        for (String prefix : prefixes) {
            for (int i = 1; i <= 100000; i++) {
                String id = prefix + String.format("%06d", i);
                String name = "test" + String.format("%07d", testCounter);
                inventoryRepository.save(new Inventory(id, name));
                testCounter++;
            }
        }
    }

    @Test
    public void testLow() {
        // 使用奈秒級計時並執行多次查詢
        int iterations = 1;

        // 單次查詢測試 - 查詢最後一筆資料 AZ100000 (test2600000)
        long startNano = System.nanoTime();
        Inventory inventory = lowInventoryRepository.findById("AZ100000");
        long endNano = System.nanoTime();

        assertEquals("test2600000", inventory.getName());

        long singleDuration = endNano - startNano;
        System.out.println("單次查詢執行時間: " + singleDuration + " 奈秒 (" + (singleDuration / 1000000.0) + " 毫秒)");

        // 多次查詢測試
        startNano = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            lowInventoryRepository.findById("AZ100000");
        }
        endNano = System.nanoTime();

        long totalDuration = endNano - startNano;
        double avgDuration = totalDuration / (double) iterations;
        System.out.println(iterations + " 次查詢總執行時間: " + totalDuration + " 奈秒 (" + (totalDuration / 1000000.0) + " 毫秒)");
        System.out.println("平均單次查詢時間: " + String.format("%.2f", avgDuration) + " 奈秒 ("
                + String.format("%.6f", avgDuration / 1000000.0) + " 毫秒)");
    }

     @Test
    public void testHigh() {
        // 使用奈秒級計時並執行多次查詢
        int iterations = 1;

        // 單次查詢測試 - 查詢最後一筆資料 AZ100000 (test2600000)
        long startNano = System.nanoTime();
        Inventory inventory = highInventoryRepository.findById("AZ100000");
        long endNano = System.nanoTime();

        assertEquals("test2600000", inventory.getName());

        long singleDuration = endNano - startNano;
        System.out.println("單次查詢執行時間: " + singleDuration + " 奈秒 (" + (singleDuration / 1000000.0) + " 毫秒)");

        // 多次查詢測試
        startNano = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            highInventoryRepository.findById("AZ100000");
        }
        endNano = System.nanoTime();

        long totalDuration = endNano - startNano;
        double avgDuration = totalDuration / (double) iterations;
        System.out.println(iterations + " 次查詢總執行時間: " + totalDuration + " 奈秒 (" + (totalDuration / 1000000.0) + " 毫秒)");
        System.out.println("平均單次查詢時間: " + String.format("%.2f", avgDuration) + " 奈秒 ("
                + String.format("%.6f", avgDuration / 1000000.0) + " 毫秒)");
    }
}
