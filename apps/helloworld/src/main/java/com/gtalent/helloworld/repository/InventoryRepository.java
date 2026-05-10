package com.gtalent.helloworld.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
}
