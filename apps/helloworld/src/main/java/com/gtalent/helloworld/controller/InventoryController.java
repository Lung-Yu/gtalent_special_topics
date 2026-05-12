package com.gtalent.helloworld.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtalent.helloworld.domain.model.Inventory;
import com.gtalent.helloworld.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/inventory?page=0&size=20&sort=id,asc */
    @GetMapping
    public Page<Inventory> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return inventoryService.findAll(pageable);
    }

    /** GET /api/inventory/{id} */
    @GetMapping("/{id}")
    public Inventory findOne(@PathVariable String id) {
        return inventoryService.findById(id);
    }

    /** POST /api/inventory */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory create(@Valid @RequestBody Inventory inventory) {
        return inventoryService.create(inventory.getId(), inventory.getName());
    }

    /** PUT /api/inventory/{id} */
    @PutMapping("/{id}")
    public Inventory update(@PathVariable String id, @RequestBody Inventory body) {
        return inventoryService.update(id, body.getName());
    }

    /** DELETE /api/inventory/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
