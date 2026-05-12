package com.gtalent.helloworld.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.Inventory;
import com.gtalent.helloworld.repository.InventoryRepository;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory create(String id, String name) {
        if (inventoryRepository.existsById(id)) {
            throw new IllegalArgumentException("庫存 ID 已存在：" + id);
        }
        return inventoryRepository.save(new Inventory(id, name));
    }

    @Transactional(readOnly = true)
    public Page<Inventory> findAll(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Inventory findById(String id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到庫存 id=" + id));
    }

    public Inventory update(String id, String name) {
        Inventory inventory = findById(id);
        inventory.setName(name);
        return inventoryRepository.save(inventory);
    }

    public void delete(String id) {
        inventoryRepository.deleteById(id);
    }
}
