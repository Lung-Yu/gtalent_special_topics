package com.example.domain.repository;

import com.example.domain.model.Inventory;

public interface InventoryRepository {

    public Inventory findById(String id);

    public void save(Inventory inventory);
    
}
